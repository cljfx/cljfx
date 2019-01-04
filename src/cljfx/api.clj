(ns cljfx.api
  (:require [cljfx.app :as app]
            [cljfx.component :as component]
            [cljfx.defaults :as defaults]
            [cljfx.fx :as fx]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.platform :as platform])
  (:import [javafx.application Platform]))

(defonce initialized
  (try
    (Platform/startup (fn []))
    :initialized
    (catch IllegalStateException _
      :already-initialized)))

(Platform/setImplicitExit false)

(def ^{:arglists '([fx-type])} keyword->lifecycle
  "Map with default lifecycles for JavaFX library

  Keywords correspond to kebab-cased class names from JavaFX library, such as `:stage`,
  `:v-box` or `:svg-path`"
  fx/keyword->lifecycle)

(defn fn->lifecycle
  "When given function, returns lifecycle that uses said function

  Using this function as part of `:fx.opt/type->lifecycle` function allows to use
  functions as fx-types to describe layout"
  [fx-type]
  (defaults/fn->lifecycle fx-type))

(defn fn->lifecycle-with-context [fx-type]
  (when (fn? fx-type) lifecycle/dynamic-fn-with-context->dynamic))

(def wrap-set-desc-as-context
  (fn [lifecycle]
    (lifecycle/wrap-desc-as-context lifecycle)))

(defn wrap-map-desc
  "Returns middleware function that applies f to passed description and passes it further"
  [f & args]
  (fn [lifecycle]
    (apply lifecycle/wrap-map-desc lifecycle f args)))

(defmacro on-fx-thread
  "Execute body (in implicit do) on fx thread

  Returns derefable with result of last expression of body. If current thread is already
  fx thread, executes body immediately"
  [& body]
  `(platform/on-fx-thread ~@body))

(defn create-component
  ([desc]
   (create-component desc {}))
  ([desc opts]
   (lifecycle/create lifecycle/root desc (defaults/fill-opts opts))))

(defn advance-component
  ([component desc]
   (advance-component component desc {}))
  ([component desc opts]
   (lifecycle/advance lifecycle/root component desc (defaults/fill-opts opts))))

(defn delete-component
  ([component]
   (delete-component component {}))
  ([component opts]
   (lifecycle/delete lifecycle/root component (defaults/fill-opts opts))))

(defn instance
  "Returns (usually mutable) Java object associated with this component"
  [component]
  (component/instance component))

(defn create-app
  "Returns a stateful function that manages lifecycle of a component

  This app function can be called from any thread with new description for a component,
  and advances component on JavaFX application thread with most actual description

  It has special semantics for `nil` descriptions meaning absence of any description, this
  is used to have full access to lifecycle functions (create/advance/delete) with single
  argument API

  Optional settings for this map:
  - `:middleware` — function that transforms used lifecycle. All such functions in
    `cljfx.api` namespace have `wrap-` prefix and can be composed with `comp`
  - `:opts` — map that every lifecycle receives as an argument and uses for various
    purposes. You can provide your data for custom lifecycles, or extend some default
    cljfx behaviour via these keys:
    - `:fx.opt/type->lifecycle` — a function that gets called on every `:fx/type` value
      in descriptions to determine what lifecycle will be used for that description
    - `:fx.opt/map-event-handler` — a function that gets called when map is used in place
      of change-listener, event-handler or any other callback-like prop. It receives that
      map with `:cljfx/event` key containing appropriate event data"
  [& {:keys [middleware opts]
      :or {middleware identity
           opts {}}}]
  (app/create middleware (defaults/fill-opts opts)))

(defn mount-app
  "Use `*ref` to provide descriptions for supplied `app` function

  This is a convenient function that adds watch to a ref + immediately calls app function
  with `deref`-ed value"
  [*ref app]
  (app/mount *ref app))
