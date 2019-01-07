(ns cljfx.component)

(defprotocol Component
  "Component is an immutable description of some (possibly mutable) object"
  :extend-via-metadata true
  (instance [this] "Returns (possibly mutable) object associated with this component"))

(extend-protocol Component
  Object
  (instance [this] this)

  nil
  (instance [this] this))