(ns cljfx.prop
  "Part of a public API

  Shape of a prop config is internal and subject to change"
  (:require [cljfx.component :as component]
            [cljfx.mutator :as mutator])
  (:import [clojure.lang IHashEq IObj Util]))

(set! *warn-on-reflection* true)

(deftype Prop [meta mutator lifecycle coerce]
  IObj
  (meta [_]
    meta)
  (withMeta [_ m]
    (Prop. m mutator lifecycle coerce))
  IHashEq
  (hasheq [_]
    (-> (Util/hasheq mutator)
        (Util/hashCombine (Util/hasheq lifecycle))
        (Util/hashCombine (Util/hasheq coerce))))
  Object
  (hashCode [_]
    (-> (Util/hash mutator)
        (Util/hashCombine (Util/hash lifecycle))
        (Util/hashCombine (Util/hash coerce))))
  (equals [_ that]
    (and (instance? Prop that)
         (let [^Prop that that]
           (and (= mutator (.-mutator that))
                (= lifecycle (.-lifecycle that))
                (= coerce (.-coerce that)))))))

(defn make
  "Creates a prop config that describes how to manage a prop value and how to assign it

  `mutator` is a Mutator that will assign prop value to a mutable java object
  `lifecycle` is a Lifecycle for a prop value whose instance will be assigned whenever it
  changes
  Additional options:
  - `:coerce` (optional, default `identity`) - last mile value transformation before
    assigning it to a mutable java object
  - `:default` (optional) - default non-coerced value that will be assigned to a mutable
    java object when prop is removed from a prop map"
  [mutator lifecycle & {:keys [coerce default]
                        :or {coerce identity
                             default ::no-default}}]
  (->Prop
    nil
    (if (= default ::no-default)
      mutator
      (mutator/wrap-default mutator default))
    lifecycle
    coerce))

(defn lifecycle [^Prop prop]
  (.-lifecycle prop))

(defn coerce [^Prop prop component]
  ((.-coerce prop) (component/instance component)))

(defn assign! [^Prop prop instance component]
  (mutator/assign! (.-mutator prop) instance (.-coerce prop) (component/instance component)))

(defn replace! [^Prop prop instance old-component new-component]
  (mutator/replace! (.-mutator prop)
                    instance
                    (.-coerce prop)
                    (component/instance old-component)
                    (component/instance new-component)))

(defn retract! [^Prop prop instance component]
  (mutator/retract! (.-mutator prop) instance (.-coerce prop) (component/instance component)))

(defn from [props-config k]
  (let [ret (get props-config k k)]
    (if (instance? Prop ret)
      ret
      (throw (ex-info (str "No such prop: " (pr-str k)) {:prop k})))))

(defn annotate
  "For cljfx.dev, annotate prop with a map describing the spec

  Examples:
    {:type :boolean}
    {:type :number}
    {:type :coll :item {:type :string}}
    {:type :desc :of 'javafx.scene.Node}
    {:type :enum :of 'javafx.stage.Modality}
    {:type :event-handler :of 'javafx.event.EventHandler}"
  [prop type-map]
  (vary-meta prop assoc :cljfx/prop type-map))
