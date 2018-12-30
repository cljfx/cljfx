# cljfx

Cljfx is a library that tries to provide reagent-like experience for
JavaFX applications.

## Introduction

### Hello world

Minimal example:
```clj
(ns example
  (:require [cljfx.api :as cljfx]))

(cljfx/on-fx-thread
  (cljfx/create-component
    [:stage {:showing true :title "Cljfx example" :width 300 :height 100}
     [:scene
      [:v-box {:alignment :center}
       [:label "Hello world"]]]]))
```
Evaluating this code will create and show this window:

![](doc/hello-world.png)

### App

To be truly useful, there should be some state and changes over time,
for this matter there is an `app` abstraction, which is basically a
function that you may call whenever you want with new hiccup-like
description, and cljfx will advance all the mutable state underneath to
fit it. Example:
```clj
(def app (cljfx/create-app))

(defn root [showing]
  [:stage {:showing showing}
   [:scene
    [:v-box {:padding 50}
     [:button
      {:text "close"
       :on-action (fn [_] (app [root false]))}]]]])

(app [root true])
```
Evaluating this code will show this:

![](doc/app-example.png)

Clicking `close` button will hide this window.

### Atoms

Example above works, but not very convenient: what we'd really like is
to have a single global state as a value in an atom, derive our
description of JavaFX state from this value, and change this atom's
contents instead. Here is how it's done:
```clj
;; Define application state

(def *state
  (atom {:title "App title"}))

;; Define render functions

(defn title-input [title]
  [:text-field
   {:on-text-changed #(swap! *state assoc :title %)
    :text title}])

(defn root [{:keys [title]}]
  [:stage {:showing true :title title}
   [:scene
    [:v-box
     [:label "Window title input"]
     [title-input title]]]])

;; Create app with middleware that maps incoming data - description -
;; to hiccup description that can be used to render JavaFX state.
;; Here description is just passed as an argument to function component.

(def app
  (cljfx/create-app
    :middleware (cljfx/wrap-map-desc (fn [desc]
                                       [root desc]))))

;; Convenient way to add watch to an atom + immediately render app

(cljfx/mount-app *state app)
```
Evaluating this code pops up this window:

![](doc/state-example.png)

Editing input then immediately updates displayed app title.

## More examples

There are various examples available in [examples](examples) folder.

## License

TBD, need to consult my employer first

## TODO

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
  - changing media should re-create media player
  instance and returning prop!

- big app with everything in it to check if/how it works (generative
  tests maybe?)
- extract-rest instead of default-props
- controlled props (mostly in controls, also stage's `:showing`)
- `:on-text-changed` handler dispatches during advancing, maybe it
  shouldn't?
- default `:managed` properties in controls
- default focus traversable of controls!
- default style classes!
- default on-x-changed prop change listeners!
- advanced docs: lifecycles, opts, contexts, `:key` in lists, pane
  metadata, etc.

## Food for thought
- wrap-factory may use some memoizing and advancing
- prop lifecycle
- should I use namespaced keywords in fx components?
- how to handle dialogs, animations and other possibly missed things?
- escape hatch to force-re-render everything?
- make jfx-media and jfx-web optional?
- update to same desc should be identical (component-vec)