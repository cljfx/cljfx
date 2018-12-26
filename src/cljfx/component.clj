(ns cljfx.component)

(defprotocol Component
  "Component is a value described by hiccup-like markup with attached instance"
  :extend-via-metadata true
  (description [this] "Returns component's description")
  (instance [this] "Returns platform instance associated with this component"))

(extend-protocol Component
  Object
  (description [this] this)
  (instance [this] this))