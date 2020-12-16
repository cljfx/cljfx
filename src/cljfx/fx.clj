(ns cljfx.fx
  "Part of a public API")

(defmacro lazy-load [sym-expr]
  `(delay @(requiring-resolve '~sym-expr)))

(def keyword->lifecycle-delay
  {;; cameras
   :parallel-camera (lazy-load cljfx.fx.parallel-camera/lifecycle)
   :perspective-camera (lazy-load cljfx.fx.perspective-camera/lifecycle)
   ;; charts
   :category-axis (lazy-load cljfx.fx.category-axis/lifecycle)
   :number-axis (lazy-load cljfx.fx.number-axis/lifecycle)
   :pie-chart-data (lazy-load cljfx.fx.pie-chart-data/lifecycle)
   :xy-chart-data (lazy-load cljfx.fx.xy-chart-data/lifecycle)
   :xy-chart-series (lazy-load cljfx.fx.xy-chart-series/lifecycle)
   :pie-chart (lazy-load cljfx.fx.pie-chart/lifecycle)
   :area-chart (lazy-load cljfx.fx.area-chart/lifecycle)
   :bar-chart (lazy-load cljfx.fx.bar-chart/lifecycle)
   :bubble-chart (lazy-load cljfx.fx.bubble-chart/lifecycle)
   :line-chart (lazy-load cljfx.fx.line-chart/lifecycle)
   :scatter-chart (lazy-load cljfx.fx.scatter-chart/lifecycle)
   :stacked-area-chart (lazy-load cljfx.fx.stacked-area-chart/lifecycle)
   :stacked-bar-chart (lazy-load cljfx.fx.stacked-bar-chart/lifecycle)
   ;; effects
   :blend (lazy-load cljfx.fx.blend/lifecycle)
   :bloom (lazy-load cljfx.fx.bloom/lifecycle)
   :box-blur (lazy-load cljfx.fx.box-blur/lifecycle)
   :color-adjust (lazy-load cljfx.fx.color-adjust/lifecycle)
   :color-input (lazy-load cljfx.fx.color-input/lifecycle)
   :displacement-map (lazy-load cljfx.fx.displacement-map/lifecycle)
   :drop-shadow (lazy-load cljfx.fx.drop-shadow/lifecycle)
   :gaussian-blur (lazy-load cljfx.fx.gaussian-blur/lifecycle)
   :glow (lazy-load cljfx.fx.glow/lifecycle)
   :image-input (lazy-load cljfx.fx.image-input/lifecycle)
   :inner-shadow (lazy-load cljfx.fx.inner-shadow/lifecycle)
   :lighting (lazy-load cljfx.fx.lighting/lifecycle)
   :light-distant (lazy-load cljfx.fx.light-distant/lifecycle)
   :light-point (lazy-load cljfx.fx.light-point/lifecycle)
   :light-spot (lazy-load cljfx.fx.light-spot/lifecycle)
   :motion-blur (lazy-load cljfx.fx.motion-blur/lifecycle)
   :perspective-transform (lazy-load cljfx.fx.perspective-transform/lifecycle)
   :reflection (lazy-load cljfx.fx.reflection/lifecycle)
   :sepia-tone (lazy-load cljfx.fx.sepia-tone/lifecycle)
   :shadow (lazy-load cljfx.fx.shadow/lifecycle)
   ;; scene
   :image-view (lazy-load cljfx.fx.image-view/lifecycle)
   :canvas (lazy-load cljfx.fx.canvas/lifecycle)
   :group (lazy-load cljfx.fx.group/lifecycle)
   :sub-scene (lazy-load cljfx.fx.sub-scene/lifecycle)
   :region (lazy-load cljfx.fx.region/lifecycle)
   :scene (lazy-load cljfx.fx.scene/lifecycle)
   :stage (lazy-load cljfx.fx.stage/lifecycle)
   ;; web
   :html-editor (lazy-load cljfx.fx.html-editor/lifecycle)
   :web-view (lazy-load cljfx.fx.web-view/lifecycle)
   ;; media
   :media (lazy-load cljfx.fx.media/lifecycle)
   :media-player (lazy-load cljfx.fx.media-player/lifecycle)
   :media-view (lazy-load cljfx.fx.media-view/lifecycle)
   ;; panes
   :pane (lazy-load cljfx.fx.pane/lifecycle)
   :anchor-pane (lazy-load cljfx.fx.anchor-pane/lifecycle)
   :border-pane (lazy-load cljfx.fx.border-pane/lifecycle)
   :flow-pane (lazy-load cljfx.fx.flow-pane/lifecycle)
   :grid-pane (lazy-load cljfx.fx.grid-pane/lifecycle)
   :row-constraints (lazy-load cljfx.fx.row-constraints/lifecycle)
   :column-constraints (lazy-load cljfx.fx.column-constraints/lifecycle)
   :h-box (lazy-load cljfx.fx.h-box/lifecycle)
   :stack-pane (lazy-load cljfx.fx.stack-pane/lifecycle)
   :text-flow (lazy-load cljfx.fx.text-flow/lifecycle)
   :tile-pane (lazy-load cljfx.fx.tile-pane/lifecycle)
   :v-box (lazy-load cljfx.fx.v-box/lifecycle)
   ;; shapes
   :arc (lazy-load cljfx.fx.arc/lifecycle)
   :circle (lazy-load cljfx.fx.circle/lifecycle)
   :cubic-curve (lazy-load cljfx.fx.cubic-curve/lifecycle)
   :ellipse (lazy-load cljfx.fx.ellipse/lifecycle)
   :line (lazy-load cljfx.fx.line/lifecycle)
   :path (lazy-load cljfx.fx.path/lifecycle)
   :arc-to (lazy-load cljfx.fx.arc-to/lifecycle)
   :close-path (lazy-load cljfx.fx.close-path/lifecycle)
   :cubic-curve-to (lazy-load cljfx.fx.cubic-curve-to/lifecycle)
   :h-line-to (lazy-load cljfx.fx.h-line-to/lifecycle)
   :line-to (lazy-load cljfx.fx.line-to/lifecycle)
   :move-to (lazy-load cljfx.fx.move-to/lifecycle)
   :quad-curve-to (lazy-load cljfx.fx.quad-curve-to/lifecycle)
   :v-line-to (lazy-load cljfx.fx.v-line-to/lifecycle)
   :polygon (lazy-load cljfx.fx.polygon/lifecycle)
   :polyline (lazy-load cljfx.fx.polyline/lifecycle)
   :quad-curve (lazy-load cljfx.fx.quad-curve/lifecycle)
   :rectangle (lazy-load cljfx.fx.rectangle/lifecycle)
   :svg-path (lazy-load cljfx.fx.svg-path/lifecycle)
   :text (lazy-load cljfx.fx.text/lifecycle)
   ;; transform
   :affine (lazy-load cljfx.fx.affine/lifecycle)
   :rotate (lazy-load cljfx.fx.rotate/lifecycle)
   :scale (lazy-load cljfx.fx.scale/lifecycle)
   :shear (lazy-load cljfx.fx.shear/lifecycle)
   :translate (lazy-load cljfx.fx.translate/lifecycle)
   ;; 3d shapes
   :box (lazy-load cljfx.fx.box/lifecycle)
   :cylinder (lazy-load cljfx.fx.cylinder/lifecycle)
   :mesh-view (lazy-load cljfx.fx.mesh-view/lifecycle)
   :triangle-mesh (lazy-load cljfx.fx.triangle-mesh/lifecycle)
   :sphere (lazy-load cljfx.fx.sphere/lifecycle)
   :ambient-light (lazy-load cljfx.fx.ambient-light/lifecycle)
   :point-light (lazy-load cljfx.fx.point-light/lifecycle)
   :phong-material (lazy-load cljfx.fx.phong-material/lifecycle)
   ;; controls
   :popup (lazy-load cljfx.fx.popup/lifecycle)
   :popup-control (lazy-load cljfx.fx.popup-control/lifecycle)
   :context-menu (lazy-load cljfx.fx.context-menu/lifecycle)
   :menu-item (lazy-load cljfx.fx.menu-item/lifecycle)
   :check-menu-item (lazy-load cljfx.fx.check-menu-item/lifecycle)
   :custom-menu-item (lazy-load cljfx.fx.custom-menu-item/lifecycle)
   :menu (lazy-load cljfx.fx.menu/lifecycle)
   :radio-menu-item (lazy-load cljfx.fx.radio-menu-item/lifecycle)
   :tooltip (lazy-load cljfx.fx.tooltip/lifecycle)
   :titled-pane (lazy-load cljfx.fx.titled-pane/lifecycle)
   :accordion (lazy-load cljfx.fx.accordion/lifecycle)
   :button-bar (lazy-load cljfx.fx.button-bar/lifecycle)
   :choice-box (lazy-load cljfx.fx.choice-box/lifecycle)
   :color-picker (lazy-load cljfx.fx.color-picker/lifecycle)
   :combo-box (lazy-load cljfx.fx.combo-box/lifecycle)
   :date-picker (lazy-load cljfx.fx.date-picker/lifecycle)
   :button (lazy-load cljfx.fx.button/lifecycle)
   :check-box (lazy-load cljfx.fx.check-box/lifecycle)
   :hyperlink (lazy-load cljfx.fx.hyperlink/lifecycle)
   :menu-button (lazy-load cljfx.fx.menu-button/lifecycle)
   :split-menu-button (lazy-load cljfx.fx.split-menu-button/lifecycle)
   :toggle-button (lazy-load cljfx.fx.toggle-button/lifecycle)
   :toggle-group (lazy-load cljfx.fx.toggle-group/lifecycle)
   :radio-button (lazy-load cljfx.fx.radio-button/lifecycle)
   :label (lazy-load cljfx.fx.label/lifecycle)
   :list-view (lazy-load cljfx.fx.list-view/lifecycle)
   :menu-bar (lazy-load cljfx.fx.menu-bar/lifecycle)
   :pagination (lazy-load cljfx.fx.pagination/lifecycle)
   :progress-indicator (lazy-load cljfx.fx.progress-indicator/lifecycle)
   :progress-bar (lazy-load cljfx.fx.progress-bar/lifecycle)
   :scroll-bar (lazy-load cljfx.fx.scroll-bar/lifecycle)
   :scroll-pane (lazy-load cljfx.fx.scroll-pane/lifecycle)
   :separator (lazy-load cljfx.fx.separator/lifecycle)
   :slider (lazy-load cljfx.fx.slider/lifecycle)
   :spinner (lazy-load cljfx.fx.spinner/lifecycle)
   :integer-spinner-value-factory (lazy-load cljfx.fx.integer-spinner-value-factory/lifecycle)
   :double-spinner-value-factory (lazy-load cljfx.fx.double-spinner-value-factory/lifecycle)
   :list-spinner-value-factory (lazy-load cljfx.fx.list-spinner-value-factory/lifecycle)
   :split-pane (lazy-load cljfx.fx.split-pane/lifecycle)
   :table-view (lazy-load cljfx.fx.table-view/lifecycle)
   :table-column (lazy-load cljfx.fx.table-column/lifecycle)
   :tab-pane (lazy-load cljfx.fx.tab-pane/lifecycle)
   :tab (lazy-load cljfx.fx.tab/lifecycle)
   :text-area (lazy-load cljfx.fx.text-area/lifecycle)
   :text-field (lazy-load cljfx.fx.text-field/lifecycle)
   :text-formatter (lazy-load cljfx.fx.text-formatter/lifecycle)
   :password-field (lazy-load cljfx.fx.password-field/lifecycle)
   :tool-bar (lazy-load cljfx.fx.tool-bar/lifecycle)
   :tree-table-view (lazy-load cljfx.fx.tree-table-view/lifecycle)
   :tree-item (lazy-load cljfx.fx.tree-item/lifecycle)
   :tree-table-column (lazy-load cljfx.fx.tree-table-column/lifecycle)
   :tree-view (lazy-load cljfx.fx.tree-view/lifecycle)
   ;; cells
   :cell (lazy-load cljfx.fx.cell/lifecycle)
   :date-cell (lazy-load cljfx.fx.date-cell/lifecycle)
   :indexed-cell (lazy-load cljfx.fx.indexed-cell/lifecycle)
   :list-cell (lazy-load cljfx.fx.list-cell/lifecycle)
   :combo-box-list-cell (lazy-load cljfx.fx.combo-box-list-cell/lifecycle)
   :text-field-list-cell (lazy-load cljfx.fx.text-field-list-cell/lifecycle)
   :table-cell (lazy-load cljfx.fx.table-cell/lifecycle)
   :table-row (lazy-load cljfx.fx.table-row/lifecycle)
   :tree-cell (lazy-load cljfx.fx.tree-cell/lifecycle)
   :tree-table-cell (lazy-load cljfx.fx.tree-table-cell/lifecycle)
   :tree-table-row (lazy-load cljfx.fx.tree-table-row/lifecycle)
   ;; dialogs
   :alert (lazy-load cljfx.fx.alert/lifecycle)
   :choice-dialog (lazy-load cljfx.fx.choice-dialog/lifecycle)
   :dialog (lazy-load cljfx.fx.dialog/lifecycle)
   :dialog-pane (lazy-load cljfx.fx.dialog-pane/lifecycle)
   :text-input-dialog (lazy-load cljfx.fx.text-input-dialog/lifecycle)
   :file-chooser (lazy-load cljfx.fx.file-chooser/lifecycle)
   ;; transitions
   :fade-transition (lazy-load cljfx.fx.fade-transition/lifecycle)
   :fill-transition (lazy-load cljfx.fx.fill-transition/lifecycle)
   :parallel-transition (lazy-load cljfx.fx.parallel-transition/lifecycle)
   :path-transition (lazy-load cljfx.fx.path-transition/lifecycle)
   :pause-transition (lazy-load cljfx.fx.pause-transition/lifecycle)
   :rotate-transition (lazy-load cljfx.fx.rotate-transition/lifecycle)
   :scale-transition (lazy-load cljfx.fx.scale-transition/lifecycle)
   :sequential-transition (lazy-load cljfx.fx.sequential-transition/lifecycle)
   :stroke-transition (lazy-load cljfx.fx.stroke-transition/lifecycle)
   :translate-transition (lazy-load cljfx.fx.translate-transition/lifecycle)})

(defn keyword->lifecycle [kw]
  (when-let [*delay (keyword->lifecycle-delay kw)]
    @*delay))
