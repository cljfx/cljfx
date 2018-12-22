(ns cljfx.component)

(defprotocol Component
  :extend-via-metadata true
  (tag [this] "Returns type identifier for this component")
  (instance [this] "Returns platform instance associated with this component"))
