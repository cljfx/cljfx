(ns cljfx.mutator
  "Part of a public API

  All Mutator implementations should be treated as Mutator protocol implementations
  only, their internals are subject to change"
  (:import [java.util Collection]
           [javafx.beans.value ObservableValue ChangeListener]
           [javafx.collections ObservableList ObservableMap ListChangeListener]
           [javafx.scene Node]))

(set! *warn-on-reflection* true)

(defprotocol Mutator
  :extend-via-metadata true
  (assign! [this instance coerce value]
    "Applies mutation to a mutable instance with a value when prop is created")
  (replace! [this instance coerce old-value new-value]
    "Replaces old value on a mutable instance with a new one when prop is advanced

    Coercion and actual mutation should happen only when values are different")
  (retract! [this instance coerce value]
    "Removes a value from a mutable instance when prop is deleted"))

(defn setter [set-fn]
  (with-meta
    [::setter set-fn]
    {`assign! (fn [_ instance coerce value]
                (set-fn instance (coerce value)))
     `replace! (fn [_ instance coerce old-value new-value]
                 (when-not (= old-value new-value)
                   (set-fn instance (coerce new-value))))
     `retract! (fn [_ instance _ _]
                 (set-fn instance nil))}))

(defn adder-remover [add! remove!]
  (reify Mutator
    (assign! [_ instance coerce value]
      (add! instance (coerce value)))
    (replace! [_ instance coerce old-value new-value]
      (when-not (= old-value new-value)
        (remove! instance (coerce old-value))
        (add! instance (coerce new-value))))
    (retract! [_ instance coerce value]
      (remove! instance (coerce value)))))

(defn property-change-listener [get-property-fn]
  (adder-remover
    #(.addListener ^ObservableValue (get-property-fn %1)
                   ^ChangeListener %2)
    #(.removeListener ^ObservableValue (get-property-fn %1)
                      ^ChangeListener %2)))

(defn list-change-listener [get-list-fn]
  (adder-remover
    #(.addListener ^ObservableList (get-list-fn %1)
                   ^ListChangeListener %2)
    #(.removeListener ^ObservableList (get-list-fn %1)
                      ^ListChangeListener %2)))

(defn observable-list [get-list-fn]
  (let [set-all! #(.setAll ^ObservableList (get-list-fn %1)
                           ^Collection %2)]
    (with-meta
      [::observable-list get-list-fn]
      {`assign! (fn [_ instance coerce value]
                  (set-all! instance (coerce value)))
       `replace! (fn [_ instance coerce old-value new-value]
                   (when-not (= old-value new-value)
                     (set-all! instance (coerce new-value))))
       `retract! (fn [_ instance _ _]
                   (set-all! instance []))})))

(defn observable-map [get-map-fn]
  (let [set-all! #(let [^ObservableMap m (get-map-fn %1)]
                    (.clear m)
                    (.putAll m %2))]
    (with-meta
      [::observable-map get-map-fn]
      {`assign! (fn [_ instance coerce value]
                  (set-all! instance (coerce value)))
       `replace! (fn [_ instance coerce old-value new-value]
                   (when-not (= old-value new-value)
                     (set-all! instance (coerce new-value))))
       `retract! (fn [_ instance _ _]
                   (set-all! instance {}))})))

(def forbidden
  (with-meta
    [::forbidden]
    {`assign! (fn [_ _ _ value]
                (throw (ex-info "Assign forbidden" {:value value})))
     `replace! (fn [_ _ _ old-value new-value]
                 (when-not (= old-value new-value)
                   (throw (ex-info "Replace forbidden"
                                   {:old-value old-value
                                    :new-value new-value}))))
     `retract! (fn [_ _ _ value]
                 (throw (ex-info "Retract forbidden" {:value value})))}))

(defn wrap-default [mutator default]
  (with-meta
    [::default mutator default]
    {`assign! (fn [_ instance coerce value]
                (assign! mutator instance coerce value))
     `replace! (fn [_ instance coerce old-value new-value]
                 (replace! mutator instance coerce old-value new-value))
     `retract! (fn [_ instance coerce value]
                 (replace! mutator instance coerce value default))}))

(defn constraint
  "This mutator re-implements javafx.scene.layout.Pane/setConstraint (which is internal)"
  [constraint-str]
  (setter
    (fn [^Node node value]
      (let [properties (.getProperties node)
            parent (.getParent node)]
        (if (nil? value)
          (.remove properties constraint-str)
          (.put properties constraint-str value))
        (when parent
          (.requestLayout parent))))))
