(ns cljfx.api
  "Main API namespace for cljfx

  Requiring this namespace starts JavaFX runtime if it wasn't previously started

  Sections:
  - JavaFX-specific helpers and info:
    - [[initialized]] - keyword indicating whether JavaFX was initialized by cljfx or not
    - [[on-fx-thread]] - run code on fx thread
    - [[run-later]] - run code asynchronously on fx thread
  - low-level component interaction:
    - [[create-component]] - creates component from description
    - [[advance-component]] - changes previously created component to new description
    - [[delete-component]] - deletes previously created/advanced component
    - [[instance]] - get JavaFX object from component
  - extension lifecycles:
    - [[ext-instance-factory]] - manually create component instance
    - [[ext-on-instance-lifecycle]] - observe created/advanced/deleted instances
    - [[ext-let-refs]] - manage component lifecycles decoupled from component tree
    - [[ext-get-ref]] - use offscreen component instance introduced by [[ext-let-refs]],
      possibly in multiple places
  - automatic component lifecycle:
    - [[keyword->lifecycle]] - component/renderer `:fx.opt/type->lifecycle` opt function
      that returns lifecycle of keyword fx-types
    - [[fn->lifecycle]] - component/renderer `:fx.opt/type->lifecycle` opt function that
      returns lifecycle of function fx-types
    - [[fn->lifecycle-with-context]] - renderer `:fx.opt/type->lifecycle` opt function
      that returns lifecycle of function fx-types that also receive context as argument.
      Used in conjunction with [[wrap-context-desc]]
    - [[wrap-map-desc]] - renderer middleware that changes received description
    - [[wrap-many]] - renderer middleware that allows multiple root descriptions instead
      of one
    - [[wrap-context-desc]] - renderer middleware that expects `context` as description
      and passes down the description tree, used with [[fn->lifecycle-with-context]]
    - [[create-renderer]] - create function that automatically handles component lifecycle
    - [[mount-renderer]] - watch `*ref` for updates and re-render component
    - [[unmount-renderer]] - stop watching `*ref` for updates and tear down component
  - context:
    - [[create-context]] - wrap map in a black box that memoizes function subscriptions to
      it
    - [[sub]] - extract value from a context and memoize it using key or function
    - [[swap-context]] - create new context using function that reuses existing cache
    - [[reset-context]] - derive new context that reuses existing cache
    - [[unbind-context]] - debug utility that releases context from dependency tracking
  - event handling:
    - [[wrap-co-effects]] - wrap event handler to replace input side effects (such as
      derefing app state) with pure functions
    - [[make-deref-co-effect]] - helper function that creates deref co-effect
    - [[wrap-effects]] - wrap event handler to replace output side effects (such as
      updating app state, performing http requests etc.) with pure functions
    - [[make-reset-effect]] - helper function that creates effect that resets atom
    - [[dispatch-effect]] - effect that allows dispatching another events
    - [[wrap-async]] - wrap event handler to perform event processing in background
  - combining it all together:
    - [[create-app]] - convenient function that combines all cljfx concepts to create
      applications with pure views, subscriptions and event handlers"
  (:require [cljfx.component :as component]
            [cljfx.context :as context]
            [cljfx.defaults :as defaults]
            [cljfx.event-handler :as event-handler]
            [cljfx.fx :as fx]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.platform :as platform]
            [cljfx.renderer :as renderer]))

(defonce
  ^{:doc "Starts JavaFX runtime and sets implicit exit to false if JavaFX wasn't started

  Either `:cljfx.platform/initialized` or `:cljfx.platform/already-initialized`"}
  initialized
  (platform/initialize))

(defmacro on-fx-thread
  "Execute body in implicit do on fx thread

  If current thread is already an fx thread, executes body immediately. Returns derefable
  with result of last expression of body."
  [& body]
  `(platform/on-fx-thread ~@body))

(defmacro run-later
  "Asynchronously execute body in implicit do on fx thread

  Will execute asynchronously even if already on fx thread. Returns derefable with result
  of last expression of body"
  [& body]
  `(platform/run-later ~@body))

(defn create-component
  "Create component from description and optional opts for manual management

  `:opts` is map that every lifecycle receives as an argument and uses for various
  purposes. You can provide your data to custom lifecycles, or extend default cljfx
  behavior via these keys:
  - `:fx.opt/type->lifecycle` — a function that gets called on every `:fx/type` value
    in descriptions to determine what lifecycle will be used for that description
  - `:fx.opt/map-event-handler` — a function that gets called when map is used in place
    of change-listener, event-handler or any other callback-like prop. It receives that
    map with `:fx/event` key containing appropriate event data"
  ([desc]
   (create-component desc {}))
  ([desc opts]
   (lifecycle/create lifecycle/root desc (defaults/fill-opts opts))))

(defn advance-component
  "Advance component created with [[create-component]] with new description and opts

  `:opts` is map that every lifecycle receives as an argument and uses for various
  purposes. You can provide your data to custom lifecycles, or extend default cljfx
  behavior via these keys:
  - `:fx.opt/type->lifecycle` — a function that gets called on every `:fx/type` value
    in descriptions to determine what lifecycle will be used for that description
  - `:fx.opt/map-event-handler` — a function that gets called when map is used in place
    of change-listener, event-handler or any other callback-like prop. It receives that
    map with `:fx/event` key containing appropriate event data"
  ([component desc]
   (advance-component component desc {}))
  ([component desc opts]
   (lifecycle/advance lifecycle/root component desc (defaults/fill-opts opts))))

(defn delete-component
  "Delete component created with [[create-component]]

  `:opts` is map that every lifecycle receives as an argument and uses for various
  purposes. You can provide your data to custom lifecycles, or extend default cljfx
  behavior via these keys:
  - `:fx.opt/type->lifecycle` — a function that gets called on every `:fx/type` value
    in descriptions to determine what lifecycle will be used for that description
  - `:fx.opt/map-event-handler` — a function that gets called when map is used in place
    of change-listener, event-handler or any other callback-like prop. It receives that
    map with `:fx/event` key containing appropriate event data"
  ([component]
   (delete-component component {}))
  ([component opts]
   (lifecycle/delete lifecycle/root component (defaults/fill-opts opts))))

(defn instance
  "Returns (usually mutable) Java object associated with this component"
  [component]
  (component/instance component))

(def ext-instance-factory
  "Extension lifecycle that allows to manually create an instance of a component

  Supported keys:
  - `:create` (required) - 0-arg function that creates new instance of a component"
  lifecycle/instance-factory)

(def ext-on-instance-lifecycle
  "Extension lifecycle that can observe created/advanced/deleted component instances

  Supported keys:
  - `:desc` (required) - description of a component, instances of which will be observed
  - `:on-created` (optional) - function that will receive instance of a component
    described by `:desc` when it's created
  - `:on-advanced` (optional) - function that will receive previous and new instance of a
    component described by `:desc` when component's instance changes (as in gets replaced
    with another instance, not changes it's content)
  - `:on-deleted` (optional) - function that will receive instance of a component
    described by `:desc` when it's deleted"
  (lifecycle/wrap-on-instance-lifecycle lifecycle/dynamic))

(def ext-let-refs
  "Extension lifecycle that decouples component lifecycles from component tree

  It manages a set of components identified by arbitrary keys, and returns another
  component: a part of component tree that can access these offscreen component instances
  via [[ext-get-ref]]. Using [[ext-let-refs]] with same keys in that scope will shadow
  previously established \"bindings\"

  Supported keys:
  - `:refs` (required) - map from arbitrary keys to component descriptions
  - `:desc` (required) - component description that will be a part of a component tree,
    has access to components created in `:refs` using [[ext-get-ref]]"
  (lifecycle/wrap-let-refs lifecycle/dynamic))

(def ext-get-ref
  "Extension lifecycle that returns component instance created by [[ext-let-refs]]

  Supported keys:
  - `:ref` (required) - component identifier used as a key in [[ext-let-ref]]'s `:refs`
    map"
  (lifecycle/get-ref :ref))

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

(defn fn->lifecycle-with-context
  "When given function, returns lifecycle that uses said function with context

  This function is supposed to be used as part of `:fx.opt/type->lifecycle` in conjunction
  with [[wrap-context-desc]] being part of middleware"
  [fx-type]
  (when (fn? fx-type) lifecycle/context-fn->dynamic))

(defn wrap-map-desc
  "Returns middleware function that applies f to passed description and passes it further"
  [f & args]
  (fn [lifecycle]
    (apply lifecycle/wrap-map-desc lifecycle f args)))

(defn wrap-many
  "Middleware function that allows to use multiple components instead of a single one"
  [lifecycle]
  (lifecycle/wrap-many lifecycle))

(defn wrap-context-desc
  "Middleware function that passes context description as option to lifecycle

  This middleware is supposed to be used in conjunction with
  [[fn->lifecycle-with-context]] which will then pass context to every function. Context
  is a black box wrapping description map, with `sub` being an interface to that black
  box"
  [lifecycle]
  (lifecycle/wrap-context-desc lifecycle))

(defn create-renderer
  "Returns a stateful function that manages lifecycle of a component

  This renderer function can be called from any thread with new description for a
  component. and advances component on JavaFX application thread with most actual
  description

  It has special semantics for `nil` descriptions meaning absence of any description, this
  is used to have full access to lifecycle functions (create/advance/delete) with single
  argument API

  Optional settings for this map:
  - `:middleware` — function that transforms used lifecycle, such as [[wrap-map-desc]],
    [[wrap-many]] or [[wrap-context-desc]]. Can be composed with `comp`
  - `:opts` — map that every lifecycle receives as an argument and uses for various
    purposes. You can provide your data to custom lifecycles, or extend default cljfx
    behavior via these keys:
    - `:fx.opt/type->lifecycle` — a function that gets called on every `:fx/type` value
      in descriptions to determine what lifecycle will be used for that description
    - `:fx.opt/map-event-handler` — a function that gets called when map is used in place
      of change-listener, event-handler or any other callback-like prop. It receives that
      map with `:fx/event` key containing appropriate event data
  - `:error-handler` - 1-argument function that will receive Throwables thrown during
    advancing, by default will print stack traces of Exceptions and re-throw Errors

  Calling renderer function with 0 arguments will re-render current state, which is useful
  during development"
  [& {:keys [middleware opts error-handler]
      :or {middleware identity
           opts {}
           error-handler renderer/default-error-handler}}]
  (renderer/create middleware (defaults/fill-opts opts) error-handler))

(defn mount-renderer
  "Use `*ref` to provide descriptions for supplied `renderer` function

  This is a convenient function that adds watch to a ref + immediately calls renderer
  function with `deref`-ed value"
  [*ref renderer]
  (renderer/mount *ref renderer))

(defn unmount-renderer
  "Stop using `*ref` to provide descriptions for renderer and tear down component tree

  This is a convenient function that removes a watch added by [[mount-renderer]] +
  immediately calls renderer function with `nil`"
  [*ref renderer]
  (renderer/unmount *ref renderer))

(defn create-context
  "Create a memoizing context for a map

  Context should be treated as a black box with `sub` being an interface to access
  context's content. Accessing content is possible via keys or subscription functions

  Key is any value except functions that will be `get` from context map

  Subscription function is a function that expects context as first argument.
  Returned value will be memoized in this context, resulting in cache lookups for
  subsequent `sub` calls on that function with same arguments.

  Cache will be reused on contexts derived by `swap-context` and `reset-context`
  to minimize recalculations. To make it efficient, all calls to `sub` by subscription
  functions are tracked, thus calling `sub` from subscription function on received context
  is not allowed after said function returns. For example, all lazy sequences that may
  call `sub` during computing of elements have to be realized."
  ([m]
   (create-context m identity))
  ([m cache-factory]
   (context/create m cache-factory)))

(defn sub
  "Subscribe to key or subscription function in this context

  Subscribing to key (which may be anything except functions) will return value
  corresponding to that key in underlying context map

  Subscription function is any function that expects context as it's first argument"
  [context k-or-f & args]
  (apply context/sub context k-or-f args))

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

(defn wrap-co-effects
  "Event handler wrapper intended to provide mutable external dependencies as immutable
  values to make event handler pure. Transforms `f` of 2 args (dependency map + event)
  to function of 1 argument (event)

  `co-effect-id->producer` is a map from arbitrary keys to zero-argument side-effecting
  functions, which are used to produce a dependency map"
  [f co-effect-id->producer]
  (event-handler/wrap-co-effects f co-effect-id->producer))

(defn make-deref-co-effect
  "Creates co-effect function that derefs a `*ref` when it's realized"
  [*ref]
  (event-handler/make-deref-co-effect *ref))

(defn wrap-effects
  "Event handler wrapper intended to execute side effects described by otherwise pure `f`

  `f` is a function of 1 argument (event) that returns data describing possible side
  effects: a series of 2-element vectors, where 1st value corresponds to key of
  side-effecting consumer in `effect-id->consumer`, and 2nd is an argument to that
  consumer

  `effect-id->consumer` is a map from arbitrary keys to 2-argument side-effecting
  functions. 1st argument is a value provided by `f`, and second is a 1-arg event
  dispatcher that can be called with new events and will eventually call `f`

  Returns function that takes event (and optionally custom dispatcher function) and
  executes side effects described by returned values of `f`"
  [f effect-id->consumer]
  (event-handler/wrap-effects f effect-id->consumer))

(defn make-reset-effect
  "Creates effect function that reset an `*atom` when this effect is triggered"
  [*atom]
  (event-handler/make-reset-effect *atom))

(def dispatch-effect
  "Effect function that dispatches another event when this effect is triggered"
  event-handler/dispatch-effect)

(defn wrap-async
  "Event handler wrapper that redirects all actual event handling to background thread

  Returned handler uses agent underneath, so using this wrapper will require call to
  [[clojure.core/shutdown-agents]] to gracefully stop JVM

  Setting `:fx/sync` to true in event will make calling this handler block until event is
  processed. This may be useful for text changed listeners in inputs that synchronise
  typed text with displayed text.

  `f` is a function that will be called with 2 args: event and event dispatcher fn that
  can be used to enqueue more events for asynchronous handling

  `agent-options` is map that is passed to [[clojure.core/agent]], has default
  `:error-handler` that will print stack traces of thrown Throwables"
  [f & {:as agent-options}]
  (event-handler/wrap-async
    f
    (defaults/fill-async-handler-options agent-options)))

(defn create-app
  "Convenient starting point for apps with pure views, subscriptions and events

  Creates renderer that is mounted on `*context` containing context created by
  [[create-context]]

  Returns map with `:renderer` and `:handler` keys containing actual renderer and event
  handler

  Accepted options:
  - `:event-handler` (required) - map event handler that should be a pure function.
    received current context at `:fx/context` key, should return effects description,
    default available effects are `:context` to set a context to a new value and
    `:dispatch` to dispatch new event. Events are handled asynchronously.
  - `:desc-fn` (required) - function receiving context and returning view description
  - `:co-effects` (optional, default `{}`) - additional co-effects map as described in
    [[wrap-co-effects]]
  - `:effects` (optional, default `{}`) - additional effects map as described in
    [[wrap-effects]]
  - `:async-agent-options` (optional, default `{}`) - agent options as described in
    [[wrap-async]]
  - `:renderer-middleware` (optional, default `identity`) - additional renderer
    middleware, such as [[wrap-many]]
  - `:renderer-error-handler` (optional, prints Exception stack traces and re-throws
    Errors by default) - 1-argument function that will receive Throwables thrown during
    advancing

  Note that since events are handled using agents, you'll need to call
  [[clojure.core/shutdown-agents]] to gracefully stop JVM"
  [*context & {:keys [event-handler
                      desc-fn
                      co-effects
                      effects
                      async-agent-options
                      renderer-middleware
                      renderer-error-handler]
               :or {co-effects {}
                    effects {}
                    async-agent-options {}
                    renderer-middleware identity
                    renderer-error-handler renderer/default-error-handler}}]
  (let [handler (-> event-handler
                    (event-handler/wrap-co-effects
                      (defaults/fill-co-effects co-effects *context))
                    (event-handler/wrap-effects
                      (defaults/fill-effects effects *context))
                    (event-handler/wrap-async
                      (defaults/fill-async-handler-options async-agent-options)))
        renderer (create-renderer
                   :error-handler renderer-error-handler
                   :middleware (comp
                                 wrap-context-desc
                                 (wrap-map-desc desc-fn)
                                 renderer-middleware)
                   :opts {:fx.opt/map-event-handler handler
                          :fx.opt/type->lifecycle #(or (keyword->lifecycle %)
                                                       (fn->lifecycle-with-context %))})]
    (mount-renderer *context renderer)
    {:renderer renderer
     :handler handler}))
