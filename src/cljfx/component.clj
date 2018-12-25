(ns cljfx.component)

(defprotocol Component
  "Component is a value described by hiccup-like markup with attached instance"
  :extend-via-metadata true
  (lifecycle [this] "Returns lifecycle of this component")
  (instance [this] "Returns platform instance associated with this component"))
