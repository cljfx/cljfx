# Changelog

All notable changes to [cljfx](https://github.com/cljfx/cljfx) will be 
documented in this file.

### [1.7.13](https://github.com/cljfx/cljfx/releases/tag/1.7.13) - 2021-01-23
- Set prop order on dialog subclasses to prevent exceptions caused by JavaFX's 
  order dependency between different setters.  

### [1.7.12](https://github.com/cljfx/cljfx/releases/tag/1.7.12) - 2021-01-01
- Bump javafx dep;
- Fix dynamic updates to combo-box items. 

### [1.7.11](https://github.com/cljfx/cljfx/releases/tag/1.7.11) - 2020-12-11
- Add WebView extension that provides WebEngine-related extra props 
  ([example](examples/e39_web_view_extensions.clj));

### [1.7.10](https://github.com/cljfx/cljfx/releases/tag/1.7.10) - 2020-09-15

Deprecate `fx/wrap-async`. This middleware only partially solved the problem of
blocking the UI thread. Since event processing still occurs sequentially on the 
agent thread, a long-running event handler would still block other events from 
being handled, leading to an unresponsive UI yet again. Another drawback is the 
increased complexity introduced by having to carefully manage `:fx/sync` flags 
(and the fact that they don't even help in all cases, e.g. `startDragAndDrop`). 
The recommended solution is to handle potentially blocking effects 
asynchronously (e.g. via an agent or a future) and to notify the UI about 
their completion by dispatching events.

Note that for preserving compatibility `fx/wrap-async` is not going to be 
removed: it will remain in cljfx forever. Deprecation status means this is no 
longer the recommended approach for event handling.  

### [1.7.9](https://github.com/cljfx/cljfx/releases/tag/1.7.9) - 2020-09-10
- Add `:pseudo-classes` node prop ([example](examples/e36_pseudo_classes.clj)).

### [1.7.8](https://github.com/cljfx/cljfx/releases/tag/1.7.8) - 2020-09-05

- Add proper `:row-factory` support for `:table-view` and `:tree-table-view` 
  (see updated [e16_cell_factories.clj](examples/e16_cell_factories.clj) 
  example).

### [1.7.7](https://github.com/cljfx/cljfx/releases/tag/1.7.7) - 2020-08-30

- Add missing `:date-cell` fx type (thanks [@ertugrulcetin](https://github.com/ertugrulcetin)!);
- Bring `:date-picker`'s `:day-cell-factory` up to par with other cell factories. 

### [1.7.6](https://github.com/cljfx/cljfx/releases/tag/1.7.6) - 2020-08-20

Allow following props to be `ifn?` instead of `fn?`:

- `:result-converter` in `:dialog`;
- `:cell-value-factory` in `:table-column`;
- `:cell-value-factory` in `:tree-table-column`.

### [1.7.5](https://github.com/cljfx/cljfx/releases/tag/1.7.5) - 2020-08-12

Deprecate `fx/sub` in favor of `fx/sub-val` and `fx/sub-ctx` (thanks [@fdeitylink](https://github.com/fdeitylink)
for kick-starting this process!). I never liked how complected the semantics of 
`fx/sub` were: it's either root key in a map (which imposes a restriction on 
context value to be a map) or function coupled to cljfx (since it has to know 
about context). This is why I split `fx/sub` responsibilities into 2 different 
functions:
- `fx/sub-val` that subscribes any function to a value wrapped in a context;
- `fx/sub-ctx` that subscribes function to a context itself (hence coupling to 
  cljfx), that is then subscribes to other functions.

See updated [Subscriptions and contexts](https://github.com/cljfx/cljfx#subscriptions-and-contexts) 
readme section.

Here is how you should migrate your code to benefit from removed complexity:
1. Replace key subscriptions with `fx/sub-val`:
    ```clj
   ;; when subscribing to keywords:
   (fx/sub ctx :users)
   ;; becomes
   (fx/sub-val ctx :users)
   
   ;; when subscribing to non-invokable keys:
   (fx/sub ctx "users")
   ;; becomes
   (fx/sub-val ctx get "users")
    ```
   The added benefit of `fx/sub-val` is ability to provide defaults or look 
   deeper in the map, since now you can use any function to extract value from 
   context:
   ```clj
   ;; added benefit: defaults
   (or (fx/sub ctx :users) [])
   ;; becomes
   (fx/sub-val ctx :users [])
   
   ;; added benefit: deep lookup
   (:name (fx/sub ctx user-by-id id))
   ;; becomes
   (fx/sub-val ctx get-in [:users id :name])
   ```
2. Replace root subscriptions with `fx/sub-val`:
   ```clj
   (fx/sub ctx)
   ;; becomes
   (fx/sub-val ctx identity)
   ```
3. Replace function subscriptions with `fx/sub-ctx`:
   ```clj
   (fx/sub ctx user-by-id 1)
   ;; becomes
   (fx/sub-ctx ctx user-by-id 1)
   ```
   The added benefit of `fx/sub-ctx` is that it allows subscribing to functions 
   that are not `fn?` (e.g. multi-methods).

Note that for preserving compatibility `fx/sub` is not going to be removed: it 
will remain in cljfx forever. Deprecation status means this is no longer the 
recommended approach to using contexts.   

### [1.7.4](https://github.com/cljfx/cljfx/releases/tag/1.7.4) - 2020-06-16
- Fix composite macros behavior in turkish locale

### [1.7.3](https://github.com/cljfx/cljfx/releases/tag/1.7.3) - 2020-05-23
- Fix `:event-filter`/`:event-handler` prop lifecycles

### [1.7.2](https://github.com/cljfx/cljfx/releases/tag/1.7.2) - 2020-05-10
- Add `nil` item support in cell factories
  
### [1.7.1](https://github.com/cljfx/cljfx/releases/tag/1.7.1) - 2020-05-10
- Add less buggy version of [cell factory](https://github.com/cljfx/cljfx#factory-props) 
  description

### [1.7.0](https://github.com/cljfx/cljfx/releases/tag/1.7.0) - 2020-05-04
- Add `cljfx.skip-javafx-initialization` java property useful for AOT-compilation

### [1.6.9](https://github.com/cljfx/cljfx/releases/tag/1.6.9) - 2020-04-27
- Simplify context implementation by using generations instead of dirtying to invalidate
  cache entries

### [1.6.8](https://github.com/cljfx/cljfx/releases/tag/1.6.8) - 2020-04-10
- Add `:on-value-changed` to Spinner and SpinnerValueFactory props

### [1.6.7](https://github.com/cljfx/cljfx/releases/tag/1.6.7) - 2020-03-21
- Add `:on-width-changed` and `:on-height-changed` scene props

### [1.6.6](https://github.com/cljfx/cljfx/releases/tag/1.6.6) - 2020-03-17
- Use JavaFX 14 on JDK 11+

### [1.6.5](https://github.com/cljfx/cljfx/releases/tag/1.6.5) - 2020-03-05
- Fix excessive dirtying of a context cache entries

### [1.6.4](https://github.com/cljfx/cljfx/releases/tag/1.6.4) - 2020-02-24
- Include node props in camera props

### [1.6.3](https://github.com/cljfx/cljfx/releases/tag/1.6.3) - 2020-02-05
- Improve error message for non-existing effects

### [1.6.2](https://github.com/cljfx/cljfx/releases/tag/1.6.2) - 2020-01-22
- Add `:on-focus-owner-changed` scene prop

### [1.6.1](https://github.com/cljfx/cljfx/releases/tag/1.6.1) - 2020-01-02
- Add `:on-focused-changed` node prop

### [1.6.0](https://github.com/cljfx/cljfx/releases/tag/1.6.0) - 2019-11-05
- Add Java 8 support

### [1.5.1](https://github.com/cljfx/cljfx/releases/tag/1.5.1) - 2019-10-25
- Support sequentials in style maps: `{:-fx-padding '(10 :px)}` → `-fx-padding: 10px`

### [1.5.0](https://github.com/cljfx/cljfx/releases/tag/1.5.0) - 2019-10-17
- Added `fx/ext-set-env` and `fx/ext-get-env` to set and get values in 
  component tree's environment

### [1.4.6](https://github.com/cljfx/cljfx/releases/tag/1.4.5) - 2019-10-11
- Added `:event-handler` to Nodes
- Added `:event-filter` and `:event-handler` to Windows

### [1.4.5](https://github.com/cljfx/cljfx/releases/tag/1.4.5) - 2019-10-09
- Bumped used JavaFX and Clojure versions

### [1.4.4](https://github.com/cljfx/cljfx/releases/tag/1.4.4) - 2019-10-03
- Added `:fx/executor` agent option to `wrap-async` that allows specifying
  custom executor

### [1.4.3](https://github.com/cljfx/cljfx/releases/tag/1.4.3) - 2019-09-23
- Fixed enum coercion in turkish locale

### [1.4.2](https://github.com/cljfx/cljfx/releases/tag/1.4.2) - 2019-09-13
- Improved error message on wrong prop key

### [1.4.1](https://github.com/cljfx/cljfx/releases/tag/1.4.1) - 2019-08-13
- Fixed context issue that manifested itself in returning stale sub values

### [1.4.0](https://github.com/cljfx/cljfx/releases/tag/1.4.0) - 2019-07-27
- Added basic animation support ([example](examples/e31_indefinite_transitions.clj))

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
