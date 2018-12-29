# cljfx

### TODO

- expand on props and composite lifecycle. What's known about them:
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

- big app with everything in it to check if/how it works (generative
  tests maybe?)
- `:on-delete` should not be a part of composite lifecycle, instead
  composite lifecycle should be composed

- extract-rest instead of default-props
- controlled props (mostly in controls)
- `:on-text-changed` handler dispatches during advancing, maybe it
  shouldn't?
- default :managed properties in controls
- pane props should be set through meta?
- default focus traversable of controls!
- default style classes!
- default on-x-changed prop change listeners!
- replace middleware with custom lifecycles?
- ctrl+z on text fields throws exceptions
- write docs

### Food for thought
- wrap-factory may use some memoizing and advancing
- prop lifecycle
- should I use namespaced keywords in fx components?
- how to handle dialogs, animations and other possibly missed things?
- escape hatch to force-re-render everything?
- make jfx-media and jfx-web optional?
- update to same desc should be identical (component-vec)