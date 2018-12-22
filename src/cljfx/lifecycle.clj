(ns cljfx.lifecycle)

(defprotocol Lifecycle
  :extend-via-metadata true
  (create [this desc] "Creates component or prop")
  (advance [this value new-desc] "Advances component or prop")
  (delete [this value] "Deletes component or prop"))

(defn create-component [desc]
  (create nil desc))

(defn advance-component [component desc]
  (advance nil component desc))

(defn delete-component [component]
  (delete nil component))