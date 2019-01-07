(ns cljfx.context
  (:require [clojure.set :as set])
  (:import [clojure.lang IPersistentMap]))

(defprotocol Cache
  "Cache protocol that copycats clojure.core.cache/CacheProtocol to make it easy to use
  core.cache while not requiring unnecessary dependency"
  (lookup [this k]
    "Retrieve the value associated with `k` if it exists, else `nil`")
  (has? [this k]
    "Checks if the cache contains a value associated with `k`")
  (hit [this k]
    "Meant to be called if cache contains a value associated with `k`")
  (miss [this k v]
    "Meant to be called if cache does **not** contain a value associated with `k`")
  (evict [this k]
    "Removes an entry from the cache"))

(extend-protocol Cache
  IPersistentMap
  (lookup [this k]
    (get this k))
  (has? [this k]
    (contains? this k))
  (hit [this _]
    this)
  (miss [this k v]
    (assoc this k v))
  (evict [this k]
    (dissoc this k)))

(defn- assert-not-leaked [cache sub-id]
  (assert (not (has? cache sub-id))
          (str (first sub-id) " is attempting to subscribe to bound context which already has value

Possible reasons:
- you return lazy seq which uses `cljfx.api/sub`
- you leaked context from subscription function's scope without unbinding it first (call `cljfx.api/unbind-context` on it in that case)")))

(defn unbind [context]
  (dissoc context :*deps :parent-sub-id))

(defn- calc-cached-value [context sub-id]
  (let [[sub-fn & args] sub-id
        *deps (atom {})
        bound-context (assoc context :*deps *deps :parent-sub-id sub-id)]
    {:value (apply sub-fn bound-context args)
     :depends-on @*deps}))

(defn sub [context k & args]
  (let [sub-id (apply vector k args)
        {:keys [*cache *deps]} context
        cache @*cache
        ret (if (fn? k)
              (if (:fx/cached (meta k))
                (-> (cond
                      (has? cache sub-id)
                      (do (swap! *cache hit sub-id)
                          cache)

                      (has? cache [::dirty sub-id])
                      (let [dirty-sub (lookup cache [::dirty sub-id])
                            deps (:depends-on dirty-sub)
                            unbound-context (unbind context)]
                        (if (= deps
                               (->> deps
                                    keys
                                    (map (juxt identity
                                               #(apply sub unbound-context %)))
                                    (into {})))
                          (swap! *cache (fn [cache]
                                          (-> cache
                                              (evict [::dirty sub-id])
                                              (miss sub-id dirty-sub))))
                          (let [v (calc-cached-value context sub-id)]
                            (swap! *cache (fn [cache]
                                            (-> cache
                                                (evict [::dirty sub-id])
                                                (miss sub-id v)))))))

                      :else
                      (swap! *cache miss sub-id (calc-cached-value context sub-id)))
                    (lookup sub-id)
                    :value)
                (apply k
                       (-> context
                           (assoc :parent-sub-id sub-id)
                           (dissoc :*deps))
                       args))
              (do
                (when (seq args)
                  (throw (ex-info "Subscribing to keys does not allow additional args"
                                  {:k k :args args})))
                (get-in context [:m k])))]
    (when *deps
      (assert-not-leaked cache (:parent-sub-id context))
      (swap! *deps (fn [deps]
                     (if (= ::context deps)
                       ::context
                       (assoc deps sub-id ret)))))
    ret))

(defn- make-reverse-deps [cache]
  (reduce (fn [acc [sub-id {:keys [depends-on]}]]
            (if (= ::context depends-on)
              (update acc ::context (fnil conj #{}) sub-id)
              (reduce (fn [acc dep-sub-id]
                        (update acc dep-sub-id (fnil conj #{}) sub-id))
                      acc
                      (keys depends-on))))
          {}
          cache))

(defn- gather-dirty-deps-impl [acc sub-ids reverse-deps]
  (reduce (fn [acc sub-id]
            (if (contains? acc sub-id)
              acc
              (-> acc
                  (conj sub-id)
                  (gather-dirty-deps-impl (reverse-deps sub-id) reverse-deps))))
          acc
          sub-ids))

(defn- gather-dirty-deps [sub-ids reverse-deps]
  (reduce (fn [acc sub-id]
            (gather-dirty-deps-impl acc (reverse-deps sub-id) reverse-deps))
          #{}
          sub-ids))

(defn invalidate-cache [cache old-m new-m]
  (let [changed-sub-ids (->> old-m
                             keys
                             (remove #(= (old-m %) (new-m %)))
                             set
                             (map vector))
        reverse-deps (make-reverse-deps cache)
        sub-ids-to-remove (set/union changed-sub-ids (reverse-deps ::context #{}))
        dirty-sub-ids (gather-dirty-deps sub-ids-to-remove reverse-deps)
        cache-with-removed-sub-ids (reduce evict cache sub-ids-to-remove)]
    (reduce (fn [acc sub-id]
              (-> acc
                  (evict sub-id)
                  (miss [::dirty sub-id] (lookup acc sub-id))))
            cache-with-removed-sub-ids
            dirty-sub-ids)))

(defn reset [context new-m]
  (let [{:keys [m *cache *deps]} context
        cache @*cache]
    (when *deps
      (assert-not-leaked cache (:parent-sub-id context))
      (reset! *deps ::context))
    {:m new-m
     :*cache (atom (invalidate-cache cache m new-m))}))

(defn swap [context f & args]
  (reset context (apply f (:m context) args)))

(defn create [m cache-factory]
  {:m m
   :*cache (atom (cache-factory {}))})

(defn clear-cache! [context]
  (swap! (:*cache context) #(reduce evict % (keys %))))