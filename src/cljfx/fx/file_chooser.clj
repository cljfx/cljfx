(ns cljfx.fx.file-chooser
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator]
            [cljfx.coerce :as coerce])
  (:import [javafx.stage FileChooser FileChooser$ExtensionFilter]))

(set! *warn-on-reflection* true)

(def props
  (composite/props FileChooser
    :initial-directory [:setter lifecycle/scalar :coerce coerce/file]
    :initial-file-name [:setter lifecycle/scalar]
    :selected-extension-filter [:setter lifecycle/scalar :coerce coerce/extension-filter]
    :title [:setter lifecycle/scalar]))

(def lifecycle
  (-> FileChooser
      (composite/describe
        :ctor []
        :props props)))
