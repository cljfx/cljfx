# cljfx

### TODO

- write docs
- escape hatch to force-re-render everything?
- expand on props and composite lifecycle
  What's known about them:
  - ctor:
    - scene requires root, root can be replaced afterwards
    - xy-chart requires axis, they can't be replaced afterwards
  - some props do not create instances, they use provided instead
    (dialog pane in dialog)
  - is it possible to inject components/lifecycles into cells? they are
    a bit different (triggered via updateItem), and instances are
    created for us, but otherwise it's just a node and we have props for
    them
  - prop in composite lifecycle may be a map or a function taking
  instance and returning prop!
- progress:
  - make special wrappers for panes using :properties
  - remove observable map

- big app with everything in it to check if/how it works (generative
  tests maybe?)
- `:on-delete` should not be a part of composite lifecycle, instead
  composite lifecycle should be composed

- extract-rest instead of default-props
- controlled props (mostly in controls)
- `:on-text-changed` handler dispatches during advancing, maybe it
  shouldn't?
- default :managed properties in controls
- make jfx-media and jfx-web optional?
- pane props should be set through meta?
- default focus traversable of controls!
- default style classes!
- default on-x-changed prop change listeners!

- update to same desc should be identical (component-vec)
- should I use more namespaced keywords?
- think about dialogs and other possibly missed things