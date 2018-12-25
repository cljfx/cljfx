# cljfx

### TODO

- escape hatch to force-re-render everything?
- decomplect props and composite lifecycle

  What's known about them:
  - ctor:
    - scene requires root, root can be replaced afterwards
    - xy-chart requires axis, they can't be replaced afterwards
  - coerce/ident are kinda weird, probably replace/advance should be "merged" into one action
  - delete/retract can't be "merged": deletion of component deletes prop values, but does not retract them
  - assign/create probably can't be merged because assign requires instance, and create may need props first
  - ":default" is kinda confusing
  - some props do not create instances, they use provided instead (dialog pane in dialog)
  - advancing with different :cljfx.opt/map-event-handler should recreate map event handlers
  - is it possible to inject components/lifecycles into cells? they are a bit different
  (triggered via updateItem), and instances are created for us, but otherwise it's just a node
  and we have props for them
- big app with everything in it to check if/how it works (generative tests maybe?)
- rethink `props`:
  - advance does not recreate map event handlers if :cljfx.opt/map-event-handler changed

- `:on-delete` should not be a part of composite lifecycle, instead composite lifecycle should be composed

- extract-rest instead of default-props
- controlled props (mostly in controls)
- `:on-text-changed` handler dispatches during advancing, maybe it shouldn't?
- default :managed properties in controls
- make jfx-media and jfx-web optional?
- pane props should be set through meta?
- default focus traversable of controls!
- default style classes!
- default on-x-changed prop change listeners!

- update to same desc should be identical (component-vec)
- should I use more namespaced keywords?
- think about dialogs and other possibly missed things