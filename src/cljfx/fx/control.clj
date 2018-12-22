(ns cljfx.fx.control
  (:require [cljfx.prop :as prop]
            [cljfx.fx.stage :as fx.stage]
            [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.coerce :as coerce]
            [cljfx.fx.scene :as fx.scene]
            [cljfx.event :as event])
  (:import [javafx.stage Popup]
           [javafx.scene.control PopupControl ContextMenu CheckMenuItem CustomMenuItem
                                 Menu RadioMenuItem Tooltip ContentDisplay OverrunStyle
                                 Accordion TitledPane ButtonBar ChoiceBox ColorPicker
                                 Control MenuItem Labeled ComboBoxBase ComboBox DatePicker
                                 ButtonBase Button CheckBox Hyperlink MenuButton
                                 SplitMenuButton ToggleButton RadioButton Label ListView
                                 SelectionMode MenuBar Pagination ProgressIndicator
                                 ScrollBar ScrollPane ScrollPane$ScrollBarPolicy Separator
                                 Slider Spinner SpinnerValueFactory
                                 SpinnerValueFactory$IntegerSpinnerValueFactory
                                 SpinnerValueFactory$DoubleSpinnerValueFactory
                                 SpinnerValueFactory$ListSpinnerValueFactory SplitPane TableView TableColumnBase TableColumn TableColumn$SortType TabPane TabPane$TabClosingPolicy TabPane$TabDragPolicy Tab TextInputControl TextArea TextField PasswordField ToolBar TreeTableView TreeSortMode TreeItem TreeTableColumn TreeTableColumn$SortType TreeView]
           [javafx.scene.text TextAlignment]
           [javafx.geometry Pos Side Orientation VPos HPos]))

(set! *warn-on-reflection* true)

(def popup
  (lifecycle.composite/describe Popup
    :ctor []
    :extends [fx.stage/popup-window]
    :default-prop [:content prop/extract-all]
    :props {:content [:list prop/component-vec]}))

(def popup-control
  (lifecycle.composite/describe PopupControl
    :ctor []
    :extends [fx.stage/popup-window]
    :props {:id [:setter prop/scalar]
            :max-height [:setter prop/scalar :coerce double :default -1.0]
            :max-width [:setter prop/scalar :coerce double :default -1.0]
            :min-height [:setter prop/scalar :coerce double :default -1.0]
            :min-width [:setter prop/scalar :coerce double :default -1.0]
            :pref-height [:setter prop/scalar :coerce double :default -1.0]
            :pref-width [:setter prop/scalar :coerce double :default -1.0]
            :style [:setter prop/scalar :coerce coerce/style :default ""]
            :style-class [:list prop/scalar :coerce #(if (string? %) [%] %)]}))

(def context-menu
  (lifecycle.composite/describe ContextMenu
    :ctor []
    :extends [popup-control]
    :default-prop [:items prop/extract-all]
    :props {:items [:list prop/component-vec]
            :on-action [:setter prop/scalar :coerce coerce/event-handler]}))


(def control
  (lifecycle.composite/describe Control
    :extends [fx.scene/region]
    :props {:context-menu [:setter prop/component]
            :tooltip [:setter prop/component]}))

(def menu-item
  (lifecycle.composite/describe MenuItem
    :ctor []
    :default-prop [:text prop/extract-single]
    :props {:accelerator [:setter prop/scalar :coerce coerce/key-combination]
            :disabled [(prop/setter (lifecycle.composite/setter MenuItem :disable))
                       prop/scalar
                       :default false]
            :graphic [:setter prop/component]
            :id [:setter prop/scalar]
            :mnemonic-parsing [:setter prop/scalar :default true]
            :on-action [:setter prop/scalar :coerce coerce/event-handler]
            :on-menu-validation [:setter prop/scalar :coerce coerce/event-handler]
            :style [:setter prop/scalar :coerce coerce/style :default ""]
            :style-class [:list prop/scalar :coerce #(if (string? %) [%] %)]
            :text [:setter prop/scalar]
            :user-data [:setter prop/scalar]
            :visible [:setter prop/scalar :default true]}))

(def labeled
  (lifecycle.composite/describe Labeled
    :extends [control]
    :props {:alignment [:setter prop/scalar :coerce (coerce/enum Pos) :default :center-left]
            :content-display [:setter prop/scalar :coerce (coerce/enum ContentDisplay)
                              :default :left]
            :ellipsis-string [:setter prop/scalar :default "..."]
            :font [:setter prop/scalar :coerce coerce/font :default :default]
            :graphic [:setter prop/component]
            :graphic-text-gap [:setter prop/scalar :coerce double :default 4]
            :line-spacing [:setter prop/scalar :coerce double :default 0]
            :mnemonic-parsing [:setter prop/scalar :default false]
            :text [:setter prop/scalar :default ""]
            :text-alignment [:setter prop/scalar :coerce (coerce/enum TextAlignment)
                             :default :left]
            :text-fill [:setter prop/scalar :coerce coerce/paint :default :black]
            :text-overrun [:setter prop/scalar :coerce (coerce/enum OverrunStyle)
                           :default :ellipsis]
            :underline [:setter prop/scalar :default false]
            :wrap-text [:setter prop/scalar :default false]}))

(def combo-box-base
  (lifecycle.composite/describe ComboBoxBase
    :extends [control]
    :props {:editable [:setter prop/scalar :default false]
            :on-action [:setter prop/scalar :coerce coerce/event-handler]
            :on-hidden [:setter prop/scalar :coerce coerce/event-handler]
            :on-hiding [:setter prop/scalar :coerce coerce/event-handler]
            :on-showing [:setter prop/scalar :coerce coerce/event-handler]
            :on-shown [:setter prop/scalar :coerce coerce/event-handler]
            :prompt-text [:setter prop/scalar]
            :value [:setter prop/scalar]}))

(def check-menu-item
  (lifecycle.composite/describe CheckMenuItem
    :ctor []
    :extends [menu-item]
    :default-prop [:text prop/extract-single]
    :props {:selected [:setter prop/scalar :default false]}))

(def custom-menu-item
  (lifecycle.composite/describe CustomMenuItem
    :ctor []
    :extends [menu-item]
    :default-prop [:content prop/extract-single]
    :props {:content [:setter prop/component]
            :hide-on-click [:setter prop/scalar :default true]}))
(def menu
  (lifecycle.composite/describe Menu
    :ctor []
    :extends [menu-item]
    :default-prop [:items prop/extract-all]
    :props {:items [:list prop/component-vec]
            :on-hidden [:setter prop/scalar :coerce coerce/event-handler]
            :on-hiding [:setter prop/scalar :coerce coerce/event-handler]
            :on-showing [:setter prop/scalar :coerce coerce/event-handler]
            :on-shown [:setter prop/scalar :coerce coerce/event-handler]}))

(def radio-menu-item
  (lifecycle.composite/describe RadioMenuItem
    :ctor []
    :extends [menu-item]
    :default-prop [:text prop/extract-single]
    :props {:selected [:setter prop/scalar :default false]}))

(def tooltip
  (lifecycle.composite/describe Tooltip
    :ctor []
    :extends [popup-control]
    :default-prop [:text prop/extract-single]
    :props {:content-display [:setter prop/scalar :coerce (coerce/enum ContentDisplay) :default :left]
            :font [:setter prop/scalar :coerce coerce/font :default :default]
            :graphic [:setter prop/component]
            :graphic-text-gap [:setter prop/scalar :coerce double :default 4.0]
            :hide-delay [:setter prop/scalar :coerce coerce/duration :default [200 :ms]]
            :show-delay [:setter prop/scalar :coerce coerce/duration :default [1 :second]]
            :show-duration [:setter prop/scalar :coerce coerce/duration :default [5 :seconds]]
            :text [:setter prop/scalar :default ""]
            :text-alignment [:setter prop/scalar :coerce (coerce/enum TextAlignment) :default :left]
            :text-overrun [:setter prop/scalar :coerce (coerce/enum OverrunStyle) :default :ellipsis]
            :wrap-text [:setter prop/scalar :default false]}))

(def titled-pane
  (lifecycle.composite/describe TitledPane
    :ctor []
    :extends [labeled]
    :default-prop [:content prop/extract-single]
    :props {:animated [:setter prop/scalar :default true]
            :collapsible [:setter prop/scalar :default true]
            :content [:setter prop/component]
            :expanded [:setter prop/scalar :default true]}))

(def accordion
  (lifecycle.composite/describe Accordion
    :ctor []
    :extends [control]
    :default-prop [:panes prop/extract-all]
    :props {:panes [:list prop/component-vec]}))

(def button-bar
  (lifecycle.composite/describe ButtonBar
    :ctor []
    :extends [control]
    :default-prop [:buttons prop/extract-all]
    :props {:button-min-width [:setter prop/scalar :coerce double]
            :button-order [:setter prop/scalar]
            :buttons [:list prop/component-vec]}))

(def choice-box
  (lifecycle.composite/describe ChoiceBox
    :ctor []
    :extends [control]
    :default-prop [:items prop/extract-all]
    :props {:converter [:setter prop/scalar :coerce coerce/string-converter]
            :items [:list prop/scalar]
            :on-action [:setter prop/scalar :coerce coerce/event-handler]
            :on-hidden [:setter prop/scalar :coerce coerce/event-handler]
            :on-hiding [:setter prop/scalar :coerce coerce/event-handler]
            :on-showing [:setter prop/scalar :coerce coerce/event-handler]
            :on-shown [:setter prop/scalar :coerce coerce/event-handler]
            :value [:setter prop/scalar]}))

(def color-picker
  (lifecycle.composite/describe ColorPicker
    :ctor []
    :extends [combo-box-base]
    :default-prop [:value prop/extract-single]
    :props {:value [:setter prop/scalar :coerce coerce/color]
            :custom-colors [:list prop/scalar :coerce #(map coerce/color %)]}))

(def combo-box
  (lifecycle.composite/describe ComboBox
    :ctor []
    :extends [combo-box-base]
    :default-prop [:items prop/extract-all]
    :props {:button-cell [:setter prop/component]
            :cell-factory [:setter prop/scalar :coerce coerce/cell-factory]
            :converter [:setter prop/scalar :coerce coerce/string-converter
                        :default :default]
            :items [:list prop/scalar]
            :placeholder [:setter prop/component]
            :visible-row-count [:setter prop/scalar :coerce int :default 10]}))

(def date-picker
  (lifecycle.composite/describe DatePicker
    :ctor []
    :extends [combo-box-base]
    :props {:chronology [:setter prop/scalar :coerce coerce/chronology :default :iso]
            :converter [:setter prop/scalar :coerce coerce/string-converter
                        :default :local-date]
            :day-cell-factory [:setter prop/scalar :coerce coerce/cell-factory]
            :show-week-numbers [:setter prop/scalar :default false]}))

(def button-base
  (lifecycle.composite/describe ButtonBase
    :extends [labeled]
    :props {:on-action [:setter prop/scalar :coerce coerce/event-handler]}))

(def button
  (lifecycle.composite/describe Button
    :ctor []
    :extends [button-base]
    :props {:cancel-button [:setter prop/scalar :default false]
            :default-button [:setter prop/scalar :default false]}))

(def check-box
  (lifecycle.composite/describe CheckBox
    :ctor []
    :extends [button-base]
    :default-prop [:selected prop/extract-single]
    :props {:allow-indeterminate [:setter prop/scalar :default false]
            :indeterminate [:setter prop/scalar :default false]
            :selected [:setter prop/scalar :default false]}))

(def hyperlink
  (lifecycle.composite/describe Hyperlink
    :ctor []
    :extends [button-base]
    :default-prop [:text prop/extract-single]
    :props {:visited [:setter prop/scalar :default false]}))

(def menu-button
  (lifecycle.composite/describe MenuButton
    :ctor []
    :extends [button-base]
    :default-prop [:items prop/extract-all]
    :props {:items [:list prop/component-vec]
            :on-hidden [:setter prop/scalar :coerce coerce/event-handler]
            :on-hiding [:setter prop/scalar :coerce coerce/event-handler]
            :on-showing [:setter prop/scalar :coerce coerce/event-handler]
            :on-shown [:setter prop/scalar :coerce coerce/event-handler]
            :popup-side [:setter prop/scalar :coerce (coerce/enum Side) :default :bottom]}))

(def split-menu-button
  (lifecycle.composite/describe SplitMenuButton
    :ctor []
    :default-prop [:items prop/extract-all]
    :extends [menu-button]))

(def toggle-button
  (lifecycle.composite/describe ToggleButton
    :ctor []
    :extends [button-base]
    :props {:selected [:setter prop/scalar :default false]
            :toggle-group [:setter prop/scalar]}))

(def radio-button
  (lifecycle.composite/describe RadioButton
    :ctor []
    :extends [toggle-button]))

(def label
  ;; TODO label has label-for prop - a component ref
  (lifecycle.composite/describe Label
    :ctor []
    :extends [labeled]
    :default-prop [:text prop/extract-single]))

(def list-view
  (lifecycle.composite/describe ListView
    :ctor []
    :extends [control]
    :default-prop [:items prop/extract-all]
    :props {:cell-factory [:setter prop/scalar]
            :editable [:setter prop/scalar :default false]
            :fixed-cell-size [:setter prop/scalar :coerce double :default -1.0]
            :items [:list prop/scalar]
            :on-edit-cancel [:setter prop/scalar :coerce coerce/event-handler]
            :on-edit-commit [:setter prop/scalar :coerce coerce/event-handler]
            :on-edit-start [:setter prop/scalar :coerce coerce/event-handler]
            :on-scroll-to [:setter prop/scalar :coerce coerce/event-handler]
            :orientation [:setter prop/scalar :coerce (coerce/enum Orientation)
                          :default :vertical]
            :placeholder [:setter prop/component]
            :selection-mode [(prop/setter
                               #(.setSelectionMode (.getSelectionModel ^ListView %1) %2))
                             prop/scalar
                             :coerce (coerce/enum SelectionMode)
                             :default :single]}))

(def menu-bar
  (lifecycle.composite/describe MenuBar
    :ctor []
    :extends [control]
    :default-prop [:menus prop/extract-all]
    :props {:menus [:list prop/component-vec]
            :use-system-menu-bar [:setter prop/scalar :default false]}))

(def pagination
  (lifecycle.composite/describe Pagination
    :ctor []
    :extends [control]
    :props {:current-page-index [:setter prop/scalar :coerce int :default 0]
            :max-page-indicator-count [:setter prop/scalar :coerce int :default 10]
            :page-count [:setter prop/scalar :coerce int :default Integer/MAX_VALUE]
            :page-factory [:setter prop/scalar :coerce coerce/page-factory]}))

(def progress-indicator
  (lifecycle.composite/describe ProgressIndicator
    :ctor []
    :extends [control]
    :default-prop [:progress prop/extract-single]
    :props {:progress [:setter prop/scalar :coerce double :default -1.0]}))

(def scroll-bar
  (lifecycle.composite/describe ScrollBar
    :ctor []
    :extends [control]
    :props {:block-increment [:setter prop/scalar :coerce double :default 10.0]
            :max [:setter prop/scalar :coerce double :default 100.0]
            :min [:setter prop/scalar :coerce double :default 0.0]
            :orientation [:setter prop/scalar :coerce (coerce/enum Orientation) :default :horizontal]
            :unit-increment [:setter prop/scalar :coerce double :default 1.0]
            :value [:setter prop/scalar :coerce double :default 0.0]
            :visible-amount [:setter prop/scalar :coerce double :default 15.0]}))

(def scroll-pane
  (lifecycle.composite/describe ScrollPane
    :ctor []
    :extends [control]
    :default-prop [:content prop/extract-single]
    :props {:content [:setter prop/component]
            :fit-to-height [:setter prop/scalar :default false]
            :fit-to-width [:setter prop/scalar :default false]
            :hbar-policy [:setter prop/scalar
                          :coerce (coerce/enum ScrollPane$ScrollBarPolicy)
                          :default :as-needed]
            :hmax [:setter prop/scalar :coerce double :default 1.0]
            :hmin [:setter prop/scalar :coerce double :default 0.0]
            :hvalue [:setter prop/scalar :coerce double :default 0.0]
            :min-viewport-height [:setter prop/scalar :coerce double :default 0.0]
            :min-viewport-width [:setter prop/scalar :coerce double :default 0.0]
            :pannable [:setter prop/scalar :default false]
            :pref-viewport-height [:setter prop/scalar :coerce double :default 0.0]
            :pref-viewport-width [:setter prop/scalar :coerce double :default 0.0]
            :vbar-policy [:setter prop/scalar
                          :coerce (coerce/enum ScrollPane$ScrollBarPolicy)
                          :default :as-needed]
            :viewport-bounds [:setter prop/scalar :coerce coerce/bounds :default 0]
            :vmax [:setter prop/scalar :coerce double :default 1.0]
            :vmin [:setter prop/scalar :coerce double :default 0.0]
            :vvalue [:setter prop/scalar :coerce double :default 0.0]}))

(def separator
  (lifecycle.composite/describe Separator
    :ctor []
    :extends [control]
    :props {:halignment [:setter prop/scalar :coerce (coerce/enum HPos) :default :center]
            :orientation [:setter prop/scalar :coerce (coerce/enum Orientation)
                          :default :horizontal]
            :valignment [:setter prop/scalar :coerce (coerce/enum VPos) :default :center]}))

(def slider
  (lifecycle.composite/describe Slider
    :ctor []
    :extends [control]
    :props {:block-increment [:setter prop/scalar :coerce double :default 10.0]
            :label-formatter [:setter prop/scalar :coerce coerce/string-converter]
            :major-tick-unit [:setter prop/scalar :coerce double :default 25.0]
            :max [:setter prop/scalar :coerce double :default 100.0]
            :min [:setter prop/scalar :coerce double :default 0.0]
            :minor-tick-count [:setter prop/scalar :coerce int :default 3]
            :orientation [:setter prop/scalar :coerce (coerce/enum Orientation)
                          :default :horizontal]
            :show-tick-labels [:setter prop/scalar :default false]
            :show-tick-marks [:setter prop/scalar :default false]
            :snap-to-ticks [:setter prop/scalar :default false]
            :value [:setter prop/scalar :coerce double :default 0.0]
            :value-changing [:setter prop/scalar :default false]}))

(def spinner
  (lifecycle.composite/describe Spinner
    :ctor []
    :extends [control]
    :props {:editable [:setter prop/scalar :default false]
            :initial-delay [:setter prop/scalar :coerce coerce/duration :default [300 :ms]]
            :prompt-text [:setter prop/scalar :default ""]
            :repeat-delay [:setter prop/scalar :default [60 :ms]]
            :value-factory [:setter prop/component]}))

(def spinner-value-factory
  (lifecycle.composite/describe SpinnerValueFactory
    :props {:converter [:setter prop/scalar :coerce coerce/string-converter]
            :value [:setter prop/scalar]
            :wrap-around [:setter prop/scalar :default false]}))

(def integer-spinner-value-factory
  (lifecycle.composite/describe SpinnerValueFactory$IntegerSpinnerValueFactory
    :ctor [:min :max]
    :extends [spinner-value-factory]
    :props {:amount-to-step-by [:setter prop/scalar :coerce int :default 1]
            :value [:setter prop/scalar :coerce int]
            :max [:setter prop/scalar :coerce int :default 100]
            :min [:setter prop/scalar :coerce int :default 0]}))

(def double-spinner-value-factory
  (lifecycle.composite/describe SpinnerValueFactory$DoubleSpinnerValueFactory
    :ctor [:min :max]
    :extends [spinner-value-factory]
    :props {:amount-to-step-by [:setter prop/scalar :coerce double :default 1]
            :value [:setter prop/scalar :coerce double]
            :max [:setter prop/scalar :coerce double :default 100]
            :min [:setter prop/scalar :coerce double :default 0]}))

(def list-spinner-value-factory
  (lifecycle.composite/describe SpinnerValueFactory$ListSpinnerValueFactory
    :ctor [:items]
    :extends [spinner-value-factory]
    :props {:items [:list prop/scalar :coerce coerce/observable-list]}))

(def split-pane
  (lifecycle.composite/describe SplitPane
    :ctor []
    :extends [control]
    :default-prop [:items prop/extract-all]
    :props {:divider-positions [:setter prop/scalar :coerce #(into-array Double/TYPE %)
                                :default []]
            :items [:list prop/component-vec]
            :orientation [:setter prop/scalar :coerce (coerce/enum Orientation)
                          :default :horizontal]}))

(def table-view
  (lifecycle.composite/describe TableView
    :ctor []
    :extends [control]
    :props {:column-resize-policy [:setter prop/scalar :coerce coerce/table-resize-policy
                                   :default :unconstrained]
            :columns [:list prop/component-vec]
            :editable [:setter prop/scalar :default false]
            :fixed-cell-size [:setter prop/scalar :coerce double :default -1.0]
            :items [:list prop/scalar]
            :on-scroll-to [:setter prop/scalar :coerce coerce/event-handler]
            :on-scroll-to-column [:setter prop/scalar :coerce coerce/event-handler]
            :on-sort [:setter prop/scalar :coerce coerce/event-handler]
            :placeholder [:setter prop/component]
            :row-factory [:setter prop/scalar]
            :selection-mode [(prop/setter
                               #(.setSelectionMode (.getSelectionModel ^TableView %1) %2))
                             prop/scalar
                             :coerce (coerce/enum SelectionMode)
                             :default :single]
            ; :sort-order [:list] ;; should be list of refs to columns
            :sort-policy [:setter prop/scalar :coerce coerce/table-sort-policy
                          :default :default]
            :table-menu-button-visible [:setter prop/scalar :default false]}))


(def table-column-base
  (lifecycle.composite/describe TableColumnBase
    :props {:columns [:list prop/component-vec]
            :comparator [:setter prop/scalar :default TableColumnBase/DEFAULT_COMPARATOR]
            :context-menu [:setter prop/component]
            :editable [:setter prop/scalar :default true]
            :graphic [:setter prop/component]
            :id [:setter prop/scalar]
            :max-width [:setter prop/scalar :coerce double :default 5000]
            :min-width [:setter prop/scalar :coerce double :default 10]
            :pref-width [:setter prop/scalar :coerce double :default 80]
            :reorderable [:setter prop/scalar :default true]
            :resizable [:setter prop/scalar :default true]
            :sort-node [:setter prop/component]
            :sortable [:setter prop/scalar :default true]
            :style [:setter prop/scalar :coerce coerce/style :default ""]
            :style-class [:list prop/scalar :coerce #(if (string? %) [%] %)]
            :text [:setter prop/scalar :default ""]
            :user-data [:setter prop/scalar]
            :visible [:setter prop/scalar :default true]}))

(def table-column
  (lifecycle.composite/describe TableColumn
    :ctor []
    :extends [table-column-base]
    :props {:cell-factory [:setter prop/scalar :default TableColumn/DEFAULT_CELL_FACTORY]
            :cell-value-factory [:setter prop/scalar :coerce coerce/table-cell-value-factory]
            :columns [:list prop/component-vec]
            :on-edit-cancel [:setter prop/scalar :coerce coerce/event-handler]
            :on-edit-commit [:setter prop/scalar :coerce coerce/event-handler] ;; has private default
            :on-edit-start [:setter prop/scalar :coerce coerce/event-handler]
            :sort-type [:setter prop/scalar :coerce (coerce/enum TableColumn$SortType) :default :ascending]}))

(def tab-pane
  (lifecycle.composite/describe TabPane
    :ctor []
    :extends [control]
    :default-prop [:tabs prop/extract-all]
    :props {:rotate-graphic [:setter prop/scalar :default false]
            :side [:setter prop/scalar :coerce (coerce/enum Side) :default :top]
            :tab-closing-policy [:setter prop/scalar
                                 :coerce (coerce/enum TabPane$TabClosingPolicy)
                                 :default :selected-tab]
            :tab-drag-policy [:setter prop/scalar
                              :coerce (coerce/enum TabPane$TabDragPolicy) :default :fixed]
            :tab-max-height [:setter prop/scalar :coerce double :default Double/MAX_VALUE]
            :tab-max-width [:setter prop/scalar :coerce double :default Double/MAX_VALUE]
            :tab-min-height [:setter prop/scalar :coerce double :default 0.0]
            :tab-min-width [:setter prop/scalar :coerce double :default 0.0]
            :tabs [:list prop/component-vec]}))

(def tab
  (lifecycle.composite/describe Tab
    :ctor []
    :props {:closable [:setter prop/scalar :default true]
            :content [:setter prop/component]
            :context-menu [:setter prop/component]
            :disable [:setter prop/scalar :default false]
            :graphic [:setter prop/component]
            :id [:setter prop/scalar]
            :on-close-request [:setter prop/scalar :coerce coerce/event-handler]
            :on-closed [:setter prop/scalar :coerce coerce/event-handler]
            :on-selection-changed [:setter prop/scalar :coerce coerce/event-handler]
            :style [:setter prop/scalar :coerce coerce/style]
            :style-class [:list prop/scalar :coerce #(if (string? %) [%] %)]
            :text [:setter prop/scalar]
            :tooltip [:setter prop/component]
            :user-data [:setter prop/scalar]}))

(def text-input-control
  (lifecycle.composite/describe TextInputControl
    :extends [control]
    :props {:editable [:setter prop/scalar :default true]
            :font [:setter prop/scalar :coerce coerce/font :default :default]
            :prompt-text [:setter prop/scalar :default ""]
            :text [:setter prop/scalar]
            :on-text-changed [(prop/property-change-listener
                                #(.textProperty ^TextField %))
                              prop/instance
                              :coerce event/make-change-listener]
            :text-formatter [:setter prop/scalar :coerce coerce/text-formatter]}))

(def text-area
  (lifecycle.composite/describe TextArea
    :ctor []
    :extends [text-input-control]
    :default-prop [:text prop/extract-single]
    :props {:pref-column-count [:setter prop/scalar :coerce int :default 40]
            :pref-row-count [:setter prop/scalar :coerce int :default 10]
            :scroll-left [:setter prop/scalar :coerce double :default 0.0]
            :scroll-top [:setter prop/scalar :coerce double :default 0.0]
            :wrap-text [:setter prop/scalar :default false]}))

(def text-field
  (lifecycle.composite/describe TextField
    :ctor []
    :extends [text-input-control]
    :default-prop [:text prop/extract-single]
    :props {:alignment [:setter prop/scalar :coerce (coerce/enum Pos) :default :center-left]
            :on-action [:setter prop/scalar :coerce coerce/event-handler]
            :pref-column-count [:setter prop/scalar :coerce int :default 12]}))

(def password-field
  (lifecycle.composite/describe PasswordField
    :ctor []
    :extends [text-field]
    :default-prop [:text prop/extract-single]))

(def tool-bar
  (lifecycle.composite/describe ToolBar
    :ctor []
    :extends [control]
    :default-prop [:items prop/extract-all]
    :props {:items [:list prop/component-vec]
            :orientation [:setter prop/scalar
                          :coerce (coerce/enum Orientation)
                          :default :horizontal]}))

(def tree-table-view
  (lifecycle.composite/describe TreeTableView
    :ctor []
    :extends [control]
    :default-prop [:columns prop/extract-all]
    :props {:column-resize-policy [:setter prop/scalar
                                   :coerce coerce/tree-table-resize-policy
                                   :default :unconstrained]
            :columns [:list prop/component-vec]
            :editable [:setter prop/scalar :default false]
            :fixed-cell-size [:setter prop/scalar :coerce double :default -1.0]
            :on-scroll-to [:setter prop/scalar :coerce coerce/event-handler]
            :on-scroll-to-column [:setter prop/scalar :coerce coerce/event-handler]
            :on-sort [:setter prop/scalar :coerce coerce/event-handler]
            :placeholder [:setter prop/component]
            :root [:setter prop/component]
            :row-factory [:setter prop/scalar]
            :selection-mode [(prop/setter
                               #(.setSelectionMode (.getSelectionModel ^TreeTableView %1) %2))
                             prop/scalar
                             :coerce (coerce/enum SelectionMode)
                             :default :single]
            :show-root [:setter prop/scalar :default true]
            :sort-mode [:setter prop/scalar :coerce (coerce/enum TreeSortMode)
                        :default :all-descendants]
            ; :sort-order [:list] ;; should be list of refs to columns
            :sort-policy [:setter prop/scalar :coerce coerce/tree-table-sort-policy
                          :default :default]
            ; :tree-column [:setter prop/component-vec] ;; should be a ref to column
            :table-menu-button-visible [:setter prop/scalar :default false]}))

(def tree-item
  (lifecycle.composite/describe TreeItem
    :ctor []
    :default-prop [:children prop/extract-all]
    :props {:children [:list prop/component-vec]
            :expanded [:setter prop/scalar :default false]
            :graphic [:setter prop/component]
            :value [:setter prop/scalar]}))

(def tree-table-column
  (lifecycle.composite/describe TreeTableColumn
    :ctor []
    :extends [table-column-base]
    :props {:cell-factory [:setter prop/scalar :default TreeTableColumn/DEFAULT_CELL_FACTORY]
            :cell-value-factory [:setter prop/scalar :coerce coerce/tree-table-cell-value-factory]
            :columns [:list prop/component-vec]
            :on-edit-cancel [:setter prop/scalar :coerce coerce/event-handler]
            :on-edit-commit [:setter prop/scalar :coerce coerce/event-handler] ;; has private default
            :on-edit-start [:setter prop/scalar :coerce coerce/event-handler]
            :sort-type [:setter prop/scalar :coerce (coerce/enum TreeTableColumn$SortType)
                        :default :ascending]}))

(def tree-view
  (lifecycle.composite/describe TreeView
    :ctor []
    :extends [control]
    :default-prop [:root prop/extract-single]
    :props {:cell-factory [:setter prop/scalar]
            :editable [:setter prop/scalar :default false]
            :fixed-cell-size [:setter prop/scalar :coerce double :default -1.0]
            :on-edit-cancel [:setter prop/scalar :coerce coerce/event-handler]
            :on-edit-commit [:setter prop/scalar :coerce coerce/event-handler]
            :on-edit-start [:setter prop/scalar :coerce coerce/event-handler]
            :on-scroll-to [:setter prop/scalar :coerce coerce/event-handler]
            :root [:setter prop/component]
            :selection-mode [(prop/setter
                               #(.setSelectionMode (.getSelectionModel ^TreeView %1) %2))
                             prop/scalar
                             :coerce (coerce/enum SelectionMode)
                             :default :single]
            :show-root [:setter prop/scalar :default true]}))

(def tag->lifecycle
  {:popup popup
   :popup-control popup-control
   :context-menu context-menu
   :menu.item/default menu-item
   :menu.item/check check-menu-item
   :menu.item/custom custom-menu-item
   :menu.item/menu menu
   :menu.item/radio radio-menu-item
   :tooltip tooltip
   :titled-pane titled-pane
   :accordion accordion
   :button-bar button-bar
   :choice-box choice-box
   :color-picker color-picker
   :combo-box combo-box
   :date-picker date-picker
   :button button
   :check-box check-box
   :hyperlink hyperlink
   :menu-button menu-button
   :split-menu-button split-menu-button
   :toggle-button toggle-button
   :radio-button radio-button
   :label label
   :list-view list-view
   :menu-bar menu-bar
   :pagination pagination
   :progress-indicator progress-indicator
   :scroll-bar scroll-bar
   :scroll-pane scroll-pane
   :separator separator
   :slider slider
   :spinner spinner
   :spinner.value-factory/integer integer-spinner-value-factory
   :spinner.value-factory/double double-spinner-value-factory
   :spinner.value-factory/list list-spinner-value-factory
   :split-pane split-pane
   :table-view table-view
   :table-column table-column
   :tab-pane tab-pane
   :tab tab
   :text-area text-area
   :text-field text-field
   :password-field password-field
   :tool-bar tool-bar
   :tree-table-view tree-table-view
   :tree-item tree-item
   :tree-table-column tree-table-column
   :tree-view tree-view})
