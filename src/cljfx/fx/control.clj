(ns cljfx.fx.control
  (:require [cljfx.prop :as prop]
            [cljfx.fx.stage :as fx.stage]
            [cljfx.lifecycle.composite :as lifecycle.composite]
            [cljfx.coerce :as coerce]
            [cljfx.fx.scene :as fx.scene]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.mutator :as mutator])
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
    :props {:content [:list lifecycle/hiccups]}))

(def popup-control
  (lifecycle.composite/describe PopupControl
    :ctor []
    :extends [fx.stage/popup-window]
    :props {:id [:setter lifecycle/scalar]
            :max-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :max-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :min-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :min-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :pref-height [:setter lifecycle/scalar :coerce double :default -1.0]
            :pref-width [:setter lifecycle/scalar :coerce double :default -1.0]
            :style [:setter lifecycle/scalar :coerce coerce/style :default ""]
            :style-class [:list lifecycle/scalar :coerce coerce/style-class]}))

(def context-menu
  (lifecycle.composite/describe ContextMenu
    :ctor []
    :extends [popup-control]
    :default-prop [:items prop/extract-all]
    :props {:items [:list lifecycle/hiccups]
            :on-action [:setter lifecycle/event-handler :coerce coerce/event-handler]}))


(def control
  (lifecycle.composite/describe Control
    :extends [fx.scene/region]
    :props {:context-menu [:setter lifecycle/hiccup]
            :tooltip [:setter lifecycle/hiccup]}))

(def menu-item
  (lifecycle.composite/describe MenuItem
    :ctor []
    :default-prop [:text prop/extract-single]
    :props {:accelerator [:setter lifecycle/scalar :coerce coerce/key-combination]
            :disabled [(mutator/setter (lifecycle.composite/setter MenuItem :disable))
                       lifecycle/scalar
                       :default false]
            :graphic [:setter lifecycle/hiccup]
            :id [:setter lifecycle/scalar]
            :mnemonic-parsing [:setter lifecycle/scalar :default true]
            :on-action [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-menu-validation [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :style [:setter lifecycle/scalar :coerce coerce/style :default ""]
            :style-class [:list lifecycle/scalar :coerce coerce/style-class]
            :text [:setter lifecycle/scalar]
            :user-data [:setter lifecycle/scalar]
            :visible [:setter lifecycle/scalar :default true]}))

(def labeled
  (lifecycle.composite/describe Labeled
    :extends [control]
    :props {:alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos) :default :center-left]
            :content-display [:setter lifecycle/scalar :coerce (coerce/enum ContentDisplay)
                              :default :left]
            :ellipsis-string [:setter lifecycle/scalar :default "..."]
            :font [:setter lifecycle/scalar :coerce coerce/font :default :default]
            :graphic [:setter lifecycle/hiccup]
            :graphic-text-gap [:setter lifecycle/scalar :coerce double :default 4]
            :line-spacing [:setter lifecycle/scalar :coerce double :default 0]
            :mnemonic-parsing [:setter lifecycle/scalar :default false]
            :text [:setter lifecycle/scalar :default ""]
            :text-alignment [:setter lifecycle/scalar :coerce (coerce/enum TextAlignment)
                             :default :left]
            :text-fill [:setter lifecycle/scalar :coerce coerce/paint :default :black]
            :text-overrun [:setter lifecycle/scalar :coerce (coerce/enum OverrunStyle)
                           :default :ellipsis]
            :underline [:setter lifecycle/scalar :default false]
            :wrap-text [:setter lifecycle/scalar :default false]}))

(def combo-box-base
  (lifecycle.composite/describe ComboBoxBase
    :extends [control]
    :props {:editable [:setter lifecycle/scalar :default false]
            :on-action [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-hidden [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-hiding [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-showing [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-shown [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :prompt-text [:setter lifecycle/scalar]
            :value [:setter lifecycle/scalar]}))

(def check-menu-item
  (lifecycle.composite/describe CheckMenuItem
    :ctor []
    :extends [menu-item]
    :default-prop [:text prop/extract-single]
    :props {:selected [:setter lifecycle/scalar :default false]}))

(def custom-menu-item
  (lifecycle.composite/describe CustomMenuItem
    :ctor []
    :extends [menu-item]
    :default-prop [:content prop/extract-single]
    :props {:content [:setter lifecycle/hiccup]
            :hide-on-click [:setter lifecycle/scalar :default true]}))
(def menu
  (lifecycle.composite/describe Menu
    :ctor []
    :extends [menu-item]
    :default-prop [:items prop/extract-all]
    :props {:items [:list lifecycle/hiccups]
            :on-hidden [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-hiding [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-showing [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-shown [:setter lifecycle/event-handler :coerce coerce/event-handler]}))

(def radio-menu-item
  (lifecycle.composite/describe RadioMenuItem
    :ctor []
    :extends [menu-item]
    :default-prop [:text prop/extract-single]
    :props {:selected [:setter lifecycle/scalar :default false]}))

(def tooltip
  (lifecycle.composite/describe Tooltip
    :ctor []
    :extends [popup-control]
    :default-prop [:text prop/extract-single]
    :props {:content-display [:setter lifecycle/scalar :coerce (coerce/enum ContentDisplay) :default :left]
            :font [:setter lifecycle/scalar :coerce coerce/font :default :default]
            :graphic [:setter lifecycle/hiccup]
            :graphic-text-gap [:setter lifecycle/scalar :coerce double :default 4.0]
            :hide-delay [:setter lifecycle/scalar :coerce coerce/duration :default [200 :ms]]
            :show-delay [:setter lifecycle/scalar :coerce coerce/duration :default [1 :second]]
            :show-duration [:setter lifecycle/scalar :coerce coerce/duration :default [5 :seconds]]
            :text [:setter lifecycle/scalar :default ""]
            :text-alignment [:setter lifecycle/scalar :coerce (coerce/enum TextAlignment) :default :left]
            :text-overrun [:setter lifecycle/scalar :coerce (coerce/enum OverrunStyle) :default :ellipsis]
            :wrap-text [:setter lifecycle/scalar :default false]}))

(def titled-pane
  (lifecycle.composite/describe TitledPane
    :ctor []
    :extends [labeled]
    :default-prop [:content prop/extract-single]
    :props {:animated [:setter lifecycle/scalar :default true]
            :collapsible [:setter lifecycle/scalar :default true]
            :content [:setter lifecycle/hiccup]
            :expanded [:setter lifecycle/scalar :default true]}))

(def accordion
  (lifecycle.composite/describe Accordion
    :ctor []
    :extends [control]
    :default-prop [:panes prop/extract-all]
    :props {:panes [:list lifecycle/hiccups]}))

(def button-bar
  (lifecycle.composite/describe ButtonBar
    :ctor []
    :extends [control]
    :default-prop [:buttons prop/extract-all]
    :props {:button-min-width [:setter lifecycle/scalar :coerce double]
            :button-order [:setter lifecycle/scalar]
            :buttons [:list lifecycle/hiccups]}))

(def choice-box
  (lifecycle.composite/describe ChoiceBox
    :ctor []
    :extends [control]
    :default-prop [:items prop/extract-all]
    :props {:converter [:setter lifecycle/scalar :coerce coerce/string-converter]
            :items [:list lifecycle/scalar]
            :on-action [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-hidden [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-hiding [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-showing [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-shown [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :value [:setter lifecycle/scalar]}))

(def color-picker
  (lifecycle.composite/describe ColorPicker
    :ctor []
    :extends [combo-box-base]
    :default-prop [:value prop/extract-single]
    :props {:value [:setter lifecycle/scalar :coerce coerce/color]
            :custom-colors [:list lifecycle/scalar :coerce (fn [x _] (map coerce/color x))]}))

(def combo-box
  (lifecycle.composite/describe ComboBox
    :ctor []
    :extends [combo-box-base]
    :default-prop [:items prop/extract-all]
    :props {:button-cell [:setter lifecycle/hiccup]
            :cell-factory [:setter lifecycle/scalar :coerce coerce/cell-factory]
            :converter [:setter lifecycle/scalar :coerce coerce/string-converter
                        :default :default]
            :items [:list lifecycle/scalar]
            :placeholder [:setter lifecycle/hiccup]
            :visible-row-count [:setter lifecycle/scalar :coerce int :default 10]}))

(def date-picker
  (lifecycle.composite/describe DatePicker
    :ctor []
    :extends [combo-box-base]
    :props {:chronology [:setter lifecycle/scalar :coerce coerce/chronology :default :iso]
            :converter [:setter lifecycle/scalar :coerce coerce/string-converter
                        :default :local-date]
            :day-cell-factory [:setter lifecycle/scalar :coerce coerce/cell-factory]
            :show-week-numbers [:setter lifecycle/scalar :default false]}))

(def button-base
  (lifecycle.composite/describe ButtonBase
    :extends [labeled]
    :props {:on-action [:setter lifecycle/event-handler :coerce coerce/event-handler]}))

(def button
  (lifecycle.composite/describe Button
    :ctor []
    :extends [button-base]
    :props {:cancel-button [:setter lifecycle/scalar :default false]
            :default-button [:setter lifecycle/scalar :default false]}))

(def check-box
  (lifecycle.composite/describe CheckBox
    :ctor []
    :extends [button-base]
    :default-prop [:selected prop/extract-single]
    :props {:allow-indeterminate [:setter lifecycle/scalar :default false]
            :indeterminate [:setter lifecycle/scalar :default false]
            :selected [:setter lifecycle/scalar :default false]}))

(def hyperlink
  (lifecycle.composite/describe Hyperlink
    :ctor []
    :extends [button-base]
    :default-prop [:text prop/extract-single]
    :props {:visited [:setter lifecycle/scalar :default false]}))

(def menu-button
  (lifecycle.composite/describe MenuButton
    :ctor []
    :extends [button-base]
    :default-prop [:items prop/extract-all]
    :props {:items [:list lifecycle/hiccups]
            :on-hidden [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-hiding [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-showing [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-shown [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :popup-side [:setter lifecycle/scalar :coerce (coerce/enum Side) :default :bottom]}))

(def split-menu-button
  (lifecycle.composite/describe SplitMenuButton
    :ctor []
    :default-prop [:items prop/extract-all]
    :extends [menu-button]))

(def toggle-button
  (lifecycle.composite/describe ToggleButton
    :ctor []
    :extends [button-base]
    :props {:selected [:setter lifecycle/scalar :default false]
            :toggle-group [:setter lifecycle/scalar]}))

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
    :props {:cell-factory [:setter lifecycle/scalar]
            :editable [:setter lifecycle/scalar :default false]
            :fixed-cell-size [:setter lifecycle/scalar :coerce double :default -1.0]
            :items [:list lifecycle/scalar]
            :on-edit-cancel [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-edit-commit [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-edit-start [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-scroll-to [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :orientation [:setter lifecycle/scalar :coerce (coerce/enum Orientation)
                          :default :vertical]
            :placeholder [:setter lifecycle/hiccup]
            :selection-mode [(mutator/setter
                               #(.setSelectionMode (.getSelectionModel ^ListView %1) %2))
                             lifecycle/scalar
                             :coerce (coerce/enum SelectionMode)
                             :default :single]}))

(def menu-bar
  (lifecycle.composite/describe MenuBar
    :ctor []
    :extends [control]
    :default-prop [:menus prop/extract-all]
    :props {:menus [:list lifecycle/hiccups]
            :use-system-menu-bar [:setter lifecycle/scalar :default false]}))

(def pagination
  (lifecycle.composite/describe Pagination
    :ctor []
    :extends [control]
    :props {:current-page-index [:setter lifecycle/scalar :coerce int :default 0]
            :max-page-indicator-count [:setter lifecycle/scalar :coerce int :default 10]
            :page-count [:setter lifecycle/scalar :coerce int :default Integer/MAX_VALUE]
            :page-factory [:setter (lifecycle/wrap-factory lifecycle/hiccup)
                           :coerce coerce/page-factory]}))

(def progress-indicator
  (lifecycle.composite/describe ProgressIndicator
    :ctor []
    :extends [control]
    :default-prop [:progress prop/extract-single]
    :props {:progress [:setter lifecycle/scalar :coerce double :default -1.0]}))

(def scroll-bar
  (lifecycle.composite/describe ScrollBar
    :ctor []
    :extends [control]
    :props {:block-increment [:setter lifecycle/scalar :coerce double :default 10.0]
            :max [:setter lifecycle/scalar :coerce double :default 100.0]
            :min [:setter lifecycle/scalar :coerce double :default 0.0]
            :orientation [:setter lifecycle/scalar :coerce (coerce/enum Orientation) :default :horizontal]
            :unit-increment [:setter lifecycle/scalar :coerce double :default 1.0]
            :value [:setter lifecycle/scalar :coerce double :default 0.0]
            :visible-amount [:setter lifecycle/scalar :coerce double :default 15.0]}))

(def scroll-pane
  (lifecycle.composite/describe ScrollPane
    :ctor []
    :extends [control]
    :default-prop [:content prop/extract-single]
    :props {:content [:setter lifecycle/hiccup]
            :fit-to-height [:setter lifecycle/scalar :default false]
            :fit-to-width [:setter lifecycle/scalar :default false]
            :hbar-policy [:setter lifecycle/scalar
                          :coerce (coerce/enum ScrollPane$ScrollBarPolicy)
                          :default :as-needed]
            :hmax [:setter lifecycle/scalar :coerce double :default 1.0]
            :hmin [:setter lifecycle/scalar :coerce double :default 0.0]
            :hvalue [:setter lifecycle/scalar :coerce double :default 0.0]
            :min-viewport-height [:setter lifecycle/scalar :coerce double :default 0.0]
            :min-viewport-width [:setter lifecycle/scalar :coerce double :default 0.0]
            :pannable [:setter lifecycle/scalar :default false]
            :pref-viewport-height [:setter lifecycle/scalar :coerce double :default 0.0]
            :pref-viewport-width [:setter lifecycle/scalar :coerce double :default 0.0]
            :vbar-policy [:setter lifecycle/scalar
                          :coerce (coerce/enum ScrollPane$ScrollBarPolicy)
                          :default :as-needed]
            :viewport-bounds [:setter lifecycle/scalar :coerce coerce/bounds :default 0]
            :vmax [:setter lifecycle/scalar :coerce double :default 1.0]
            :vmin [:setter lifecycle/scalar :coerce double :default 0.0]
            :vvalue [:setter lifecycle/scalar :coerce double :default 0.0]}))

(def separator
  (lifecycle.composite/describe Separator
    :ctor []
    :extends [control]
    :props {:halignment [:setter lifecycle/scalar :coerce (coerce/enum HPos) :default :center]
            :orientation [:setter lifecycle/scalar :coerce (coerce/enum Orientation)
                          :default :horizontal]
            :valignment [:setter lifecycle/scalar :coerce (coerce/enum VPos) :default :center]}))

(def slider
  (lifecycle.composite/describe Slider
    :ctor []
    :extends [control]
    :props {:block-increment [:setter lifecycle/scalar :coerce double :default 10.0]
            :label-formatter [:setter lifecycle/scalar :coerce coerce/string-converter]
            :major-tick-unit [:setter lifecycle/scalar :coerce double :default 25.0]
            :max [:setter lifecycle/scalar :coerce double :default 100.0]
            :min [:setter lifecycle/scalar :coerce double :default 0.0]
            :minor-tick-count [:setter lifecycle/scalar :coerce int :default 3]
            :orientation [:setter lifecycle/scalar :coerce (coerce/enum Orientation)
                          :default :horizontal]
            :show-tick-labels [:setter lifecycle/scalar :default false]
            :show-tick-marks [:setter lifecycle/scalar :default false]
            :snap-to-ticks [:setter lifecycle/scalar :default false]
            :value [:setter lifecycle/scalar :coerce double :default 0.0]
            :on-value-changed [(mutator/property-change-listener
                                 #(.valueProperty ^Slider %))
                               (lifecycle/wrap-coerce lifecycle/event-handler
                                                      coerce/change-listener)]
            :value-changing [:setter lifecycle/scalar :default false]}))

(def spinner
  (lifecycle.composite/describe Spinner
    :ctor []
    :extends [control]
    :props {:editable [:setter lifecycle/scalar :default false]
            :initial-delay [:setter lifecycle/scalar :coerce coerce/duration :default [300 :ms]]
            :prompt-text [:setter lifecycle/scalar :default ""]
            :repeat-delay [:setter lifecycle/scalar :default [60 :ms]]
            :value-factory [:setter lifecycle/hiccup]}))

(def spinner-value-factory
  (lifecycle.composite/describe SpinnerValueFactory
    :props {:converter [:setter lifecycle/scalar :coerce coerce/string-converter]
            :value [:setter lifecycle/scalar]
            :wrap-around [:setter lifecycle/scalar :default false]}))

(def integer-spinner-value-factory
  (lifecycle.composite/describe SpinnerValueFactory$IntegerSpinnerValueFactory
    :ctor [:min :max]
    :extends [spinner-value-factory]
    :props {:amount-to-step-by [:setter lifecycle/scalar :coerce int :default 1]
            :value [:setter lifecycle/scalar :coerce int]
            :max [:setter lifecycle/scalar :coerce int :default 100]
            :min [:setter lifecycle/scalar :coerce int :default 0]}))

(def double-spinner-value-factory
  (lifecycle.composite/describe SpinnerValueFactory$DoubleSpinnerValueFactory
    :ctor [:min :max]
    :extends [spinner-value-factory]
    :props {:amount-to-step-by [:setter lifecycle/scalar :coerce double :default 1]
            :value [:setter lifecycle/scalar :coerce double]
            :max [:setter lifecycle/scalar :coerce double :default 100]
            :min [:setter lifecycle/scalar :coerce double :default 0]}))

(def list-spinner-value-factory
  (lifecycle.composite/describe SpinnerValueFactory$ListSpinnerValueFactory
    :ctor [:items]
    :extends [spinner-value-factory]
    :props {:items [:list lifecycle/scalar :coerce coerce/observable-list]}))

(def split-pane
  (lifecycle.composite/describe SplitPane
    :ctor []
    :extends [control]
    :default-prop [:items prop/extract-all]
    :props {:divider-positions [:setter lifecycle/scalar
                                :coerce (fn [x _] (into-array Double/TYPE x))
                                :default []]
            :items [:list lifecycle/hiccups]
            :orientation [:setter lifecycle/scalar :coerce (coerce/enum Orientation)
                          :default :horizontal]}))

(def table-view
  (lifecycle.composite/describe TableView
    :ctor []
    :extends [control]
    :props {:column-resize-policy [:setter lifecycle/scalar :coerce coerce/table-resize-policy
                                   :default :unconstrained]
            :columns [:list lifecycle/hiccups]
            :editable [:setter lifecycle/scalar :default false]
            :fixed-cell-size [:setter lifecycle/scalar :coerce double :default -1.0]
            :items [:list lifecycle/scalar]
            :on-scroll-to [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-scroll-to-column [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-sort [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :placeholder [:setter lifecycle/hiccup]
            :row-factory [:setter lifecycle/scalar]
            :selection-mode [(mutator/setter
                               #(.setSelectionMode (.getSelectionModel ^TableView %1) %2))
                             lifecycle/scalar
                             :coerce (coerce/enum SelectionMode)
                             :default :single]
            ; :sort-order [:list] ;; should be list of refs to columns
            :sort-policy [:setter lifecycle/scalar :coerce coerce/table-sort-policy
                          :default :default]
            :table-menu-button-visible [:setter lifecycle/scalar :default false]}))

(def table-column-base
  (lifecycle.composite/describe TableColumnBase
    :props {:columns [:list lifecycle/hiccups]
            :comparator [:setter lifecycle/scalar :default TableColumnBase/DEFAULT_COMPARATOR]
            :context-menu [:setter lifecycle/hiccup]
            :editable [:setter lifecycle/scalar :default true]
            :graphic [:setter lifecycle/hiccup]
            :id [:setter lifecycle/scalar]
            :max-width [:setter lifecycle/scalar :coerce double :default 5000]
            :min-width [:setter lifecycle/scalar :coerce double :default 10]
            :pref-width [:setter lifecycle/scalar :coerce double :default 80]
            :reorderable [:setter lifecycle/scalar :default true]
            :resizable [:setter lifecycle/scalar :default true]
            :sort-node [:setter lifecycle/hiccup]
            :sortable [:setter lifecycle/scalar :default true]
            :style [:setter lifecycle/scalar :coerce coerce/style :default ""]
            :style-class [:list lifecycle/scalar :coerce coerce/style-class]
            :text [:setter lifecycle/scalar :default ""]
            :user-data [:setter lifecycle/scalar]
            :visible [:setter lifecycle/scalar :default true]}))

(def table-column
  (lifecycle.composite/describe TableColumn
    :ctor []
    :extends [table-column-base]
    :props {:cell-factory [:setter lifecycle/scalar :default TableColumn/DEFAULT_CELL_FACTORY]
            :cell-value-factory [:setter lifecycle/scalar :coerce coerce/table-cell-value-factory]
            :columns [:list lifecycle/hiccups]
            :on-edit-cancel [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-edit-commit [:setter lifecycle/event-handler :coerce coerce/event-handler] ;; has private default
            :on-edit-start [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :sort-type [:setter lifecycle/scalar :coerce (coerce/enum TableColumn$SortType) :default :ascending]}))

(def tab-pane
  (lifecycle.composite/describe TabPane
    :ctor []
    :extends [control]
    :default-prop [:tabs prop/extract-all]
    :props {:rotate-graphic [:setter lifecycle/scalar :default false]
            :side [:setter lifecycle/scalar :coerce (coerce/enum Side) :default :top]
            :tab-closing-policy [:setter lifecycle/scalar
                                 :coerce (coerce/enum TabPane$TabClosingPolicy)
                                 :default :selected-tab]
            :tab-drag-policy [:setter lifecycle/scalar
                              :coerce (coerce/enum TabPane$TabDragPolicy) :default :fixed]
            :tab-max-height [:setter lifecycle/scalar :coerce double :default Double/MAX_VALUE]
            :tab-max-width [:setter lifecycle/scalar :coerce double :default Double/MAX_VALUE]
            :tab-min-height [:setter lifecycle/scalar :coerce double :default 0.0]
            :tab-min-width [:setter lifecycle/scalar :coerce double :default 0.0]
            :tabs [:list lifecycle/hiccups]}))

(def tab
  (lifecycle.composite/describe Tab
    :ctor []
    :default-prop [:content prop/extract-single]
    :props {:closable [:setter lifecycle/scalar :default true]
            :content [:setter lifecycle/hiccup]
            :context-menu [:setter lifecycle/hiccup]
            :disable [:setter lifecycle/scalar :default false]
            :graphic [:setter lifecycle/hiccup]
            :id [:setter lifecycle/scalar]
            :on-close-request [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-closed [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-selection-changed [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :style [:setter lifecycle/scalar :coerce coerce/style]
            :style-class [:list lifecycle/scalar :coerce coerce/style-class]
            :text [:setter lifecycle/scalar]
            :tooltip [:setter lifecycle/hiccup]
            :user-data [:setter lifecycle/scalar]}))

(def text-input-control
  (lifecycle.composite/describe TextInputControl
    :extends [control]
    :props {:editable [:setter lifecycle/scalar :default true]
            :font [:setter lifecycle/scalar :coerce coerce/font :default :default]
            :prompt-text [:setter lifecycle/scalar :default ""]
            :text [(mutator/setter (fn [^TextInputControl control text]
                                     (when-not (= text (.getText control))
                                       (.setText control text))))
                   lifecycle/scalar]
            :on-text-changed [(mutator/property-change-listener
                                #(.textProperty ^TextField %))
                              (lifecycle/wrap-coerce lifecycle/event-handler
                                                     coerce/change-listener)]
            :text-formatter [:setter lifecycle/scalar :coerce coerce/text-formatter]}))

(def text-area
  (lifecycle.composite/describe TextArea
    :ctor []
    :extends [text-input-control]
    :default-prop [:text prop/extract-single]
    :props {:pref-column-count [:setter lifecycle/scalar :coerce int :default 40]
            :pref-row-count [:setter lifecycle/scalar :coerce int :default 10]
            :scroll-left [:setter lifecycle/scalar :coerce double :default 0.0]
            :scroll-top [:setter lifecycle/scalar :coerce double :default 0.0]
            :wrap-text [:setter lifecycle/scalar :default false]}))

(def text-field
  (lifecycle.composite/describe TextField
    :ctor []
    :extends [text-input-control]
    :default-prop [:text prop/extract-single]
    :props {:alignment [:setter lifecycle/scalar :coerce (coerce/enum Pos) :default :center-left]
            :on-action [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :pref-column-count [:setter lifecycle/scalar :coerce int :default 12]}))

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
    :props {:items [:list lifecycle/hiccups]
            :orientation [:setter lifecycle/scalar
                          :coerce (coerce/enum Orientation)
                          :default :horizontal]}))

(def tree-table-view
  (lifecycle.composite/describe TreeTableView
    :ctor []
    :extends [control]
    :default-prop [:columns prop/extract-all]
    :props {:column-resize-policy [:setter lifecycle/scalar
                                   :coerce coerce/tree-table-resize-policy
                                   :default :unconstrained]
            :columns [:list lifecycle/hiccups]
            :editable [:setter lifecycle/scalar :default false]
            :fixed-cell-size [:setter lifecycle/scalar :coerce double :default -1.0]
            :on-scroll-to [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-scroll-to-column [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-sort [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :placeholder [:setter lifecycle/hiccup]
            :root [:setter lifecycle/hiccup]
            :row-factory [:setter lifecycle/scalar]
            :selection-mode [(mutator/setter
                               #(.setSelectionMode (.getSelectionModel ^TreeTableView %1) %2))
                             lifecycle/scalar
                             :coerce (coerce/enum SelectionMode)
                             :default :single]
            :show-root [:setter lifecycle/scalar :default true]
            :sort-mode [:setter lifecycle/scalar :coerce (coerce/enum TreeSortMode)
                        :default :all-descendants]
            ; :sort-order [:list] ;; should be list of refs to columns
            :sort-policy [:setter lifecycle/scalar :coerce coerce/tree-table-sort-policy
                          :default :default]
            ; :tree-column [:setter lifecycle/many-dynamic-hiccups] ;; should be a ref to column
            :table-menu-button-visible [:setter lifecycle/scalar :default false]}))

(def tree-item
  (lifecycle.composite/describe TreeItem
    :ctor []
    :default-prop [:children prop/extract-all]
    :props {:children [:list lifecycle/hiccups]
            :expanded [:setter lifecycle/scalar :default false]
            :graphic [:setter lifecycle/hiccup]
            :value [:setter lifecycle/scalar]}))

(def tree-table-column
  (lifecycle.composite/describe TreeTableColumn
    :ctor []
    :extends [table-column-base]
    :props {:cell-factory [:setter lifecycle/scalar :default TreeTableColumn/DEFAULT_CELL_FACTORY]
            :cell-value-factory [:setter lifecycle/scalar :coerce coerce/tree-table-cell-value-factory]
            :columns [:list lifecycle/hiccups]
            :on-edit-cancel [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-edit-commit [:setter lifecycle/event-handler :coerce coerce/event-handler] ;; has private default
            :on-edit-start [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :sort-type [:setter lifecycle/scalar :coerce (coerce/enum TreeTableColumn$SortType)
                        :default :ascending]}))

(def tree-view
  (lifecycle.composite/describe TreeView
    :ctor []
    :extends [control]
    :default-prop [:root prop/extract-single]
    :props {:cell-factory [:setter lifecycle/scalar]
            :editable [:setter lifecycle/scalar :default false]
            :fixed-cell-size [:setter lifecycle/scalar :coerce double :default -1.0]
            :on-edit-cancel [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-edit-commit [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-edit-start [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :on-scroll-to [:setter lifecycle/event-handler :coerce coerce/event-handler]
            :root [:setter lifecycle/hiccup]
            :selection-mode [(mutator/setter
                               #(.setSelectionMode (.getSelectionModel ^TreeView %1) %2))
                             lifecycle/scalar
                             :coerce (coerce/enum SelectionMode)
                             :default :single]
            :show-root [:setter lifecycle/scalar :default true]}))

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
