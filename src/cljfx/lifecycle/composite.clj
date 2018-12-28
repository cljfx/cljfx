(ns cljfx.lifecycle.composite
  (:require [cljfx.component :as component]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator]
            [cljfx.prop :as prop]
            [clojure.string :as str]))

(defn- desc->props-desc [desc component-config]
  (let [args? (next desc)
        props? (first args?)
        has-props? (map? props?)
        props (if has-props? props? {})
        args (if has-props? (next args?) args?)
        [default-prop extract-fn] (:default-prop component-config)]
    (cond-> props
      args (assoc default-prop (extract-fn args)))))

(defn- create-props [props-desc props-config opts]
  (reduce
    (fn [acc k]
      (let [prop-config (get props-config k)]
        (when-not prop-config
          (throw (ex-info "No such prop" {:key k :desc (get props-desc k)})))
        (assoc acc k (lifecycle/create (prop/lifecycle prop-config)
                                       (get props-desc k)
                                       opts))))
    props-desc
    (keys props-desc)))

(defn- create-composite-component [this desc opts]
  (let [props-desc (desc->props-desc desc this)
        props-config (:props this)
        props (create-props props-desc props-config opts)
        args (:args this)
        instance (apply (:ctor this) (map #(prop/coerce (props-config %) (props %)) args))
        arg-set (set args)
        sorted-props (if-let [prop-order (:prop-order this)]
                       (sort-by #(get prop-order (key %) 0)
                                props)
                       props)]
    (doseq [[k v] sorted-props
            :when (not (contains? arg-set k))]
      (prop/assign! (get props-config k) instance v))
    (with-meta {:props props
                :desc desc
                :instance instance}
               {`component/instance :instance
                `component/description :desc})))

(defn- advance-composite-component [this component desc opts]
  (let [props-desc (desc->props-desc desc this)
        props-config (:props this)
        instance (component/instance component)]
    (-> component
        (assoc :desc desc)
        (update
          :props
          (fn [props]
            (let [prop-keys (set (concat (keys props) (keys props-desc)))
                  sorted-prop-keys (if-let [prop-order (:prop-order this)]
                                     (sort-by #(get prop-order % 0) prop-keys)
                                     prop-keys)]
              (reduce
                (fn [acc k]
                  (let [old-e (find props k)
                        new-e (find props-desc k)]
                    (cond
                      (and (some? old-e) (some? new-e))
                      (let [old-component (val old-e)
                            desc (val new-e)
                            prop-config (get props-config k)
                            new-component (lifecycle/advance (prop/lifecycle prop-config)
                                                             old-component
                                                             desc
                                                             opts)]
                        (prop/replace! prop-config instance old-component new-component)
                        (assoc acc k new-component))

                      (some? old-e)
                      (let [prop-config (get props-config k)]
                        (prop/retract! prop-config instance (val old-e))
                        (lifecycle/delete (prop/lifecycle prop-config) (val old-e) opts)
                        (dissoc acc k))

                      :else
                      (let [prop-config (get props-config k)
                            component (lifecycle/create (prop/lifecycle prop-config)
                                                        (val new-e)
                                                        opts)]
                        (prop/assign! prop-config instance component)
                        (assoc acc k component)))))
                props
                sorted-prop-keys)))))))

(defn- delete-composite-component [this component opts]
  (let [props-config (:props this)]
    (doseq [[k v] (:props component)]
      (lifecycle/delete (prop/lifecycle (get props-config k)) v opts))
    (when-let [on-delete (:on-delete this)]
      (on-delete (component/instance component)))))

(defn component [m]
  (with-meta
    m
    {`lifecycle/create create-composite-component
     `lifecycle/advance advance-composite-component
     `lifecycle/delete delete-composite-component}))

(defmacro setter [type-expr kw]
  (let [instance-sym (with-meta (gensym "instance") {:tag type-expr})
        value-sym (gensym "value")
        setter-expr (symbol (apply str ".set" (map str/capitalize (-> kw
                                                                      name
                                                                      (str/split #"-")))))
        fn-name (symbol (str "set-" (name kw)))]
    `(fn ~fn-name [~instance-sym ~value-sym]
       (~setter-expr ~instance-sym ~value-sym))))

(defmacro observable-list [type-expr kw]
  (let [instance-sym (with-meta (gensym "instance") {:tag type-expr})
        getter-expr (symbol (apply str ".get" (map str/capitalize (-> kw
                                                                      name
                                                                      (str/split #"-")))))
        fn-name (symbol (str "get-" (name kw)))]
    `(fn ~fn-name [~instance-sym]
       (~getter-expr ~instance-sym))))

(defmacro prop-map [type-expr & kvs]
  `(hash-map
     ~@(->> kvs
            (partition 2)
            (mapcat
              (fn [[k v]]
                (when-not (vector? v)
                  (throw (ex-info "Prop description should be a vector"
                                  {:k k :v v})))
                (let [[mutator & args] v
                      prop `(prop/make
                              ~(case mutator
                                 :setter `(mutator/setter (setter ~type-expr ~k))
                                 :list `(mutator/observable-list
                                          (observable-list ~type-expr ~k))
                                 mutator)
                              ~@args)]
                  [k prop]))))))

(defmacro describe [type-expr & kvs]
  (let [kv-map (apply hash-map kvs)
        wrap-lifecycle (if (contains? kv-map :ctor)
                         (fn [x] `(component ~x))
                         identity)
        extend-bindings (->> kv-map
                             :extends
                             (map (juxt (fn [_] (gensym "extend"))
                                        identity))
                             (into {}))
        wrap-props (if (empty? extend-bindings)
                     identity
                     (fn [x]
                       `(merge ~@(->> extend-bindings
                                      keys
                                      (map (fn [k]
                                             `(:props ~k))))
                               ~x)))]
    (wrap-lifecycle
      `(let [~@(mapcat identity extend-bindings)]
         (hash-map ~@(mapcat
                       (fn [[k v]]
                         (case k
                           :ctor
                           (let [args (map #(-> % name gensym) v)
                                 ctor-sym (symbol (str type-expr "."))]
                             `[:ctor (fn [~@args]
                                       (~ctor-sym ~@args))
                               :args ~v])

                           :extends []
                           :props []

                           [k v]))
                       kv-map)
                   :props ~(let [prop-map-expr `(prop-map
                                                  ~type-expr
                                                  ~@(mapcat identity (:props kv-map)))]
                             (wrap-props prop-map-expr)))))))