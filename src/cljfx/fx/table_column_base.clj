(ns cljfx.fx.table-column-base
  (:require [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control TableColumnBase]))

(set! *warn-on-reflection* true)

(def lifecycle
  (lifecycle.composite/describe TableColumnBase
    :props {:columns [:list lifecycle/dynamics]
            :comparator [:setter lifecycle/scalar :default TableColumnBase/DEFAULT_COMPARATOR]
            :context-menu [:setter lifecycle/dynamic]
            :editable [:setter lifecycle/scalar :default true]
            :graphic [:setter lifecycle/dynamic]
            :id [:setter lifecycle/scalar]
            :max-width [:setter lifecycle/scalar :coerce double :default 5000]
            :min-width [:setter lifecycle/scalar :coerce double :default 10]
            :pref-width [:setter lifecycle/scalar :coerce double :default 80]
            :reorderable [:setter lifecycle/scalar :default true]
            :resizable [:setter lifecycle/scalar :default true]
            :sort-node [:setter lifecycle/dynamic]
            :sortable [:setter lifecycle/scalar :default true]
            :style [:setter lifecycle/scalar :coerce coerce/style :default ""]
            :style-class [:list lifecycle/scalar :coerce coerce/style-class]
            :text [:setter lifecycle/scalar :default ""]
            :user-data [:setter lifecycle/scalar]
            :visible [:setter lifecycle/scalar :default true]}))