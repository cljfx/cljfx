(ns cljfx.fx.text-formatter
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control TextFormatter]
           [java.util.function UnaryOperator]
           [javafx.util StringConverter]))

(defn- unary-operator [x]
  (cond
    (instance? UnaryOperator x)
    x

    (fn? x)
    (reify UnaryOperator
      (apply [_ v]
        (x v)))

    :else
    (coerce/fail UnaryOperator x)))

(defn- nilable [coerce]
  (fn [x]
    (if (nil? x)
      nil
      (coerce x))))

(def props
  (composite/props TextFormatter
    :value [:setter lifecycle/scalar]
    :on-value-changed [:property-change-listener lifecycle/change-listener]
    :value-converter [mutator/forbidden lifecycle/scalar
                      :coerce (nilable coerce/string-converter)]
    :filter [mutator/forbidden lifecycle/scalar
             :coerce (nilable unary-operator)]))

(def lifecycle
  (composite/lifecycle
    {:props props
     :args [:value-converter :filter]
     :ctor (fn [^StringConverter value-converter ^UnaryOperator filter]
             (cond
               (and value-converter filter)
               (TextFormatter. value-converter nil filter)

               value-converter
               (TextFormatter. value-converter)

               filter
               (TextFormatter. filter)

               :else
               (throw (ex-info "Can't construct TextFormatter"
                               {:value-converter value-converter
                                :filter filter}))))}))
