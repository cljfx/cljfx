(ns cljfx.context
  "Part of a public API

  Context value should be treated a black box, it's shape is subject to change"
  (:import [clojure.lang IPersistentMap Seqable]
           [java.io FileNotFoundException]))

(defprotocol Cache
  "Cache protocol that copycats clojure.core.cache/CacheProtocol to make it easy to use
  core.cache while not requiring unnecessary dependency if context is unused"
  (lookup [this k]
    "Retrieve the value associated with `k` if it exists, else `nil`")
  (has? [this k]
    "Checks if the cache contains a value associated with `k`")
  (hit [this k]
    "Meant to be called if cache contains a value associated with `k`")
  (miss [this k v]
    "Meant to be called if cache does **not** contain a value associated with `k`")
  (evict [this k]
    "Removes an entry from the cache"))

(extend-protocol Cache
  IPersistentMap
  (lookup [this k]
    (get this k))
  (has? [this k]
    (contains? this k))
  (hit [this _]
    this)
  (miss [this k v]
    (assoc this k v))
  (evict [this k]
    (dissoc this k)))

(def ^:private ->cache
  (try
    (let [core-cache-protocol @(requiring-resolve 'clojure.core.cache/CacheProtocol)
          core-cache-lookup @(requiring-resolve 'clojure.core.cache/lookup)
          core-cache-has? @(requiring-resolve 'clojure.core.cache/has?)
          core-cache-hit @(requiring-resolve 'clojure.core.cache/hit)
          core-cache-miss @(requiring-resolve 'clojure.core.cache/miss)
          core-cache-evict @(requiring-resolve 'clojure.core.cache/evict)
          f (fn wrap [cache]
              (reify
                Cache
                (lookup [_ k] (core-cache-lookup cache k))
                (has? [_ k] (core-cache-has? cache k))
                (hit [this k]
                  (let [new-cache (core-cache-hit cache k)]
                    (if (identical? new-cache cache)
                      this
                      (wrap new-cache))))
                (miss [_ k v]
                  (wrap (core-cache-miss cache k v)))
                (evict [this k]
                  (let [new-cache (core-cache-evict cache k)]
                    (if (identical? new-cache cache)
                      this
                      (wrap new-cache))))
                Seqable
                (seq [_] (seq cache))))]
      #(if (satisfies? core-cache-protocol %)
         (f %)
         %))
    (catch FileNotFoundException _
      identity)))

(defn- assert-not-leaked [generation cache sub-id]
  (let [e (lookup cache sub-id)]
    (assert (or (nil? e) (not= generation (::generation e)))
            (str (first sub-id) " is attempting to subscribe to bound context which already has value

Possible reasons:
- you return lazy seq which uses `cljfx.api/sub` while calculating elements
- you leaked context from subscription function's scope without unbinding it first (call `cljfx.api/unbind-context` on it in that case)"))))

(defn unbind [context]
  (dissoc context ::*deps ::parent-sub-id))

(defn- calc-cache-entry [context sub-id]
  (let [[sub-fn & args] sub-id
        *deps (atom {})
        bound-context (assoc context ::parent-sub-id sub-id ::*deps *deps)
        value (apply sub-fn bound-context args)]
    {::value value
     ::generation (::generation context)
     ::deps @*deps}))

(declare sub)

(defn- sub-from-dirty [context *cache dirty-entry sub-id]
  (let [deps (::deps dirty-entry)
        unbound-context (unbind context)
        e (if (and (not= deps ::context)
                   (every? #(= (get deps %) (apply sub unbound-context %)) (keys deps)))
            (assoc dirty-entry ::generation (::generation context))
            (calc-cache-entry context sub-id))]
    (swap! *cache miss sub-id e)
    e))

(defn- add-dep [deps k v]
  (if (= ::context deps)
    ::context
    (assoc deps k v)))

(defn- keys-sub [ctx & ks]
  (get-in (sub ctx) (vec ks)))

(defn sub
  ([context]
   (let [{::keys [m *cache *deps generation]} context
         cache @*cache]
     (when *deps
       (assert-not-leaked generation cache (::parent-sub-id context))
       (reset! *deps ::context))
     m))
  ([context k & args]
   (let [sub-id (if (fn? k)
                  (apply vector k args)
                  (apply vector keys-sub k args))
         {::keys [*cache *deps generation]} context
         cache @*cache
         existing-entry (lookup cache sub-id)
         entry (cond
                 (nil? existing-entry)
                 (let [e (calc-cache-entry context sub-id)]
                   (swap! *cache miss sub-id e)
                   e)

                 (= generation (::generation existing-entry))
                 (do (swap! *cache hit sub-id)
                     existing-entry)

                 :else
                 (sub-from-dirty context *cache existing-entry sub-id))
         ret (::value entry)]
     (when *deps
       (assert-not-leaked generation cache (::parent-sub-id context))
       (swap! *deps add-dep sub-id ret))
     ret)))

(defn reset [context new-m]
  (let [{::keys [*cache *deps generation]} context
        cache @*cache]
    (when *deps
      (assert-not-leaked generation cache (::parent-sub-id context))
      (reset! *deps ::context))
    {::m new-m
     ::generation (inc generation)
     ::*cache (atom cache)}))

(defn swap [context f & args]
  (reset context (apply f (::m context) args)))

(defn create [m cache-factory]
  {::m m
   ::generation 0
   ::*cache (atom (->cache (cache-factory {})))})

(defn clear-cache! [context]
  (swap! (::*cache context) #(reduce evict % (keys %))))

(defn ^:deprecated ensure-dirty
  "Accidentally public remains of the old context implementation, do not use"
  [sub-id]
  (case (sub-id 0)
    ::dirty sub-id
    [::dirty sub-id]))
