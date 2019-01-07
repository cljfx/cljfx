(ns cljfx.api
  (:require [cljfx.app :as app]
            [cljfx.component :as component]
            [cljfx.defaults :as defaults]
            [cljfx.fx :as fx]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.platform :as platform]
            [cljfx.context :as context])
  (:import [javafx.application Platform]))

(defonce initialized
  (try
    (Platform/startup (fn []))
    :initialized
    (catch IllegalStateException _
      :already-initialized)))

(Platform/setImplicitExit false)

(defn keyword->lifecycle
  "When given fitting keyword, returns lifecycle for corresponding JavaFX class

  Fitting keywords are kebab-cased class names from JavaFX library, such as `:stage`,
  `:v-box` or `:svg-path`"
  [fx-type]
  (fx/keyword->lifecycle fx-type))

(defn fn->lifecycle
  "When given function, returns lifecycle that uses said function

  Using this function as part of `:fx.opt/type->lifecycle` function allows to use
  functions as fx-types to describe layout"
  [fx-type]
  (defaults/fn->lifecycle fx-type))

(defn wrap-map-desc
  "Returns middleware function that applies f to passed description and passes it further"
  [f & args]
  (fn [lifecycle]
    (apply lifecycle/wrap-map-desc lifecycle f args)))

(defn wrap-many
  "Middleware function that allows to use multiple components instead of a single one"
  [lifecycle]
  (lifecycle/wrap-many lifecycle))

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
      map with `:fx/event` key containing appropriate event data

  Calling app function with 0 arguments will re-render current state, which is useful
  during development"
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

(defn create-context
  "Create a memoizing context for a map

  Context should be treated as a black box with `sub` being an interface to access
  context's content. Accessing content is possible via keys or subscription functions

  Key is any value except functions that will be `get` from context map

  Subscription function is a function that expects context as first argument.
  If `:fx/cached` is `true` on such function's metadata, returned value will be memoized
  in this context, resulting in cache lookups for subsequent `sub` calls on that function.

  Cache will be reused on contexts derived by `swap-context` and `reset-context`
  to minimize recalculations. To make it efficient, all calls to `sub` by subscription
  functions are tracked, thus calling `sub` from subscription function on received context
  is not allowed after said function returns. For example, all lazy sequences that may
  call `sub` during computing of elements have to be realized."
  ([m]
   (create-context m identity))
  ([m cache-factory]
   (context/create m cache-factory)))

(defn swap-context
  "Create new context with context map being (apply f current-map args), reusing existing
  cache"
  [context f & args]
  (apply context/swap context f args))

(defn reset-context
  "Create new context with context map being m, reusing existing cache"
  [context m]
  (context/reset context m))

(defn unbind-context
  "Frees context from tracking subscription functions

  During debugging it may be useful to save context from subscription function to some
  temporary state and then explore it. In that case context should be freed from tracking
  debugged subscription function by calling `unbind-context` on it"
  [context]
  (context/unbind context))

(defn sub
  "Subscribe to key or subscription function in this context

  Subscribing to key (which may be anything except functions) will return value
  corresponding to that key in underlying context map

  Subscription function is any function that expects context as it's first argument"
  [context k-or-f & args]
  (apply context/sub context k-or-f args))

(defn fn->lifecycle-with-context
  "When given function, returns lifecycle that uses said function with context

  This function is supposed to be used as part of `:fx.opt/type->lifecycle` in conjunction
  with `wrap-context-desc` being part of middleware"
  [fx-type]
  (when (fn? fx-type) lifecycle/context-fn->dynamic))

(defn wrap-context-desc
  "Middleware function that passes context description as option to lifecycle

  This middleware is supposed to be used in conjunction with `fn->lifecycle-with-context`
  which will then pass context to every function. Context is a black box wrapping
  description map, with `sub` being an interface to that black box"
  [lifecycle]
  (lifecycle/wrap-context-desc lifecycle))
