(ns cljfx.component)

(defprotocol Component
  :extend-via-metadata true
  (lifecycle [this] "Returns lifecycle of this component")
  (instance [this] "Returns platform instance associated with this component"))
