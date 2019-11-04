(ns cljfx.fx.table-column-base
  "Part of a public API"
  (:require [cljfx.composite :as composite]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.jdk.fx.table-column-base :as jdk.fx.table-column-base]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.control TableColumnBase]))

(set! *warn-on-reflection* true)

(def props
  (merge
    jdk.fx.table-column-base/props
    (composite/props TableColumnBase
      :columns [:list lifecycle/dynamics]
      :comparator [:setter lifecycle/scalar :default TableColumnBase/DEFAULT_COMPARATOR]
      :context-menu [:setter lifecycle/dynamic]
      :editable [:setter lifecycle/scalar :default true]
      :graphic [:setter lifecycle/dynamic]
      :id [:setter lifecycle/scalar]
      :max-width [:setter lifecycle/scalar :coerce double :default 5000]
      :min-width [:setter lifecycle/scalar :coerce double :default 10]
      :pref-width [:setter lifecycle/scalar :coerce double :default 80]
      :resizable [:setter lifecycle/scalar :default true]
      :sort-node [:setter lifecycle/dynamic]
      :sortable [:setter lifecycle/scalar :default true]
      :style [:setter lifecycle/scalar :coerce coerce/style :default ""]
      :style-class [:list lifecycle/scalar :coerce coerce/style-class]
      :text [:setter lifecycle/scalar :default ""]
      :user-data [:setter lifecycle/scalar]
      :visible [:setter lifecycle/scalar :default true])))
