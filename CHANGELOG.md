# Changelog

All notable changes to [cljfx](https://github.com/cljfx/cljfx) will be 
documented in this file.

### [1.3.5](https://github.com/cljfx/cljfx/releases/tag/1.3.5) - 2019-07-11
- Allow `:selected-item` in extra-props lifecycles to be descriptions

### [1.3.4](https://github.com/cljfx/cljfx/releases/tag/1.3.4) - 2019-07-03
- Added `:event-filter` prop to all nodes ([example](examples/e30_devtools_via_event_filters.clj))

### [1.3.3](https://github.com/cljfx/cljfx/releases/tag/1.3.3) - 2019-06-20
- Added `:on-tabs-changed` prop to `:tab-pane` to observe reordering of tabs

### [1.3.2](https://github.com/cljfx/cljfx/releases/tag/1.3.2) - 2019-05-31
- Added window listener props `:on-x-changed`, `:on-y-changed`,
  `:on-width-changed` and `:on-height-changed`
- Made `cljfx.fx.canvas/props` prop map include parent props too

### [1.3.1](https://github.com/cljfx/cljfx/releases/tag/1.3.1) - 2019-05-28
- Fixed a bug with `:shadow` fx-type keyword pointing to sepia-tone lifecycle
- Added TextFormatter support ([example](examples/e29_text_formatter.clj))

### [1.3.0](https://github.com/cljfx/cljfx/releases/tag/1.3.0) - 2019-05-23
- Added lifecycles allowing extra props for ultimate extensibility
- Added commonly useful extra-props extension lifecycles (see
  [selection models](examples/e27_selection_models.clj) and
  [tooltip](examples/e26_tooltips.clj) examples)

### [1.2.13](https://github.com/cljfx/cljfx/releases/tag/1.2.13) - 2019-05-22
- Added `fx/ext-many` extension lifecycle that is preferred over `fx/wrap-many`
  renderer middleware

### [1.2.12](https://github.com/cljfx/cljfx/releases/tag/1.2.12) - 2019-05-20
- Added `:renderer-error-handler` option to `fx/create-app`

### [1.2.11](https://github.com/cljfx/cljfx/releases/tag/1.2.11) - 2019-05-20
- Added `:error-handler` option to `fx/create-renderer`

### [1.2.10](https://github.com/cljfx/cljfx/releases/tag/1.2.10) - 2019-05-16
- Brought back accidentally disabled `:image-view` props: `:y`, `:fit-width`,
  `:fit-height`, `:preserve-ratio`, `:smooth` and `:viewport`

### [1.2.9](https://github.com/cljfx/cljfx/releases/tag/1.2.9) - 2019-05-14
- Added `:clip` prop on Node

### [1.2.8](https://github.com/cljfx/cljfx/releases/tag/1.2.8) - 2019-04-17
- Added `:on-focused-changed` prop for Window class

### [1.2.7](https://github.com/cljfx/cljfx/releases/tag/1.2.7) - 2019-04-15
- Added `:owner` prop for Stage and Dialog classes

### [1.2.6](https://github.com/cljfx/cljfx/releases/tag/1.2.6) - 2019-04-05
- Added `:stylesheets` support in components extending Parent

### [1.2.5](https://github.com/cljfx/cljfx/releases/tag/1.2.5) - 2019-03-31
- Added ToggleGroup support
- Allowed specifying `:toggle-group` in ToggleButton's descriptions declaratively ([example](examples/e25_radio_buttons.clj))

### [1.2.4](https://github.com/cljfx/cljfx/releases/tag/1.2.4) - 2019-03-30
- Added `:install-to` synthetic prop (deprecated!) for Tooltips to allow
  declarative tooltip installation to non-control components
  ([example](examples/e26_tooltips.clj))
- Added `fx/unmount-renderer` function to remove watch from `*ref` that was
  added by `fx/mount-renderer` and then tear down component tree

### [1.2.3](https://github.com/cljfx/cljfx/releases/tag/1.2.3) - 2019-03-20
- Improved responsiveness in the presence of flood of description changes,
  see [examples/e24_concurrency.clj](examples/e24_concurrency.clj)

### [1.2.2](https://github.com/cljfx/cljfx/releases/tag/1.2.2) - 2019-03-19
- Updated JavaFX dependencies to latest stable release
- Stopped warning on reflection where reflection is unavoidable

### [1.2.1](https://github.com/cljfx/cljfx/releases/tag/1.2.1) - 2019-03-17
- Added support `:accelerators` prop in scene ([example](examples/e23_accelerators.clj))

### [1.2.0](https://github.com/cljfx/cljfx/releases/tag/1.2.0) - 2019-03-12
- Added `fx/ext-let-refs` and `fx/ext-get-ref` [extension lifecycles](https://github.com/cljfx/cljfx#included-extension-lifecycles)
  that allow decoupling component lifecycle from component tree 

### [1.1.0](https://github.com/cljfx/cljfx/releases/tag/1.1.0) - 2019-03-03
- Added treating fx-type as `Lifecycle` if `:fx.opt/type->lifecycle` returned
  falsey value;
- Add some basic [extension lifecycles](https://github.com/cljfx/cljfx#extending-cljfx): 
  - `fx/ext-instance-factory` to create component instances using 
    factory function;
  - `fx/ext-on-instance-lifecycle` to observe created/advanced/deleted
    component instances.   

### [1.0.0](https://github.com/cljfx/cljfx/releases/tag/1.0.0) - 2019-02-24
Initial release
