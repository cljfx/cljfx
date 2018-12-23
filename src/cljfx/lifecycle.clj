(ns cljfx.lifecycle)

(defprotocol Lifecycle
  :extend-via-metadata true
  (create [this desc opts] "Creates component or prop")
  (advance [this value new-desc opts] "Advances component or prop")
  (delete [this value opts] "Deletes component or prop"))

(defn create-component [desc opts]
  (create nil desc opts))

(defn advance-component [component desc opts]
  (advance nil component desc opts))

(defn delete-component [component opts]
  (delete nil component opts))