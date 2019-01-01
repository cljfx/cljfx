(ns cljfx.mutator
  (:import [java.util Collection]
           [javafx.beans.value ObservableValue ChangeListener]
           [javafx.collections ObservableList]))

(set! *warn-on-reflection* true)

(defprotocol Mutator
  :extend-via-metadata true
  (assign! [this instance coerce value])
  (replace! [this instance coerce old-value new-value])
  (retract! [this instance coerce value]))

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

(defn property-change-listener [get-property-fn]
  (let [add! #(.addListener ^ObservableValue (get-property-fn %1)
                            ^ChangeListener %2)
        remove! #(.removeListener ^ObservableValue (get-property-fn %1)
                                  ^ChangeListener %2)]
    (with-meta
      [::property-change-listener get-property-fn]
      {`assign! (fn [_ instance coerce value]
                  (add! instance (coerce value)))
       `replace! (fn [_ instance coerce old-value new-value]
                   (when-not (= old-value new-value)
                     (remove! instance (coerce old-value))
                     (add! instance (coerce new-value))))
       `retract! (fn [_ instance coerce value]
                   (remove! instance (coerce value)))})))

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