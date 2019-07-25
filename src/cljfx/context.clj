(ns cljfx.context
  "Part of a public API

  Context value should be treated a black box, it's shape is subject to change"
  (:require [clojure.set :as set])
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

(defn- assert-not-leaked [cache sub-id & data-flat]
  (when (has? cache sub-id)
    (throw (ex-info 
             (str (first sub-id) " is attempting to subscribe to bound context which already has value

Possible reasons:
- you return lazy seq which uses `cljfx.api/sub` while calculating elements
- you leaked context from subscription function's scope without unbinding it first (call `cljfx.api/unbind-context` on it in that case)")
             (into (apply hash-map data-flat) {:parent-sub-id sub-id})))))

(defn unbind [context]
  (dissoc context ::*direct-deps ::*key-deps ::parent-sub-id))

(defn- calc-cache-entry [context sub-id]
  (let [[sub-fn & args] sub-id
        *direct-deps (atom {})
        *key-deps (atom #{})
        bound-context (assoc context
                        ::parent-sub-id sub-id
                        ::*direct-deps *direct-deps
                        ::*key-deps *key-deps)
        value (apply sub-fn bound-context args)]
    {::value value
     ::direct-deps @*direct-deps
     ::key-deps @*key-deps}))

(declare sub register-cache-entry)

(def ^:dynamic *processing-dirty* #{})

(defn- sub-from-dirty [context *cache cache sub-id]
  (let [dirty-sub (lookup cache [::dirty sub-id])
        unbound-context (unbind context)
        key-deps (reduce
                   (fn [key-deps [dep-id dep-v]]
                     (let [v (-> (register-cache-entry unbound-context *cache @*cache dep-id)
                                 (lookup dep-id))]
                       (if (not= dep-v (::value v))
                         (reduced nil)
                         (into key-deps (::key-deps v)))))
                   (::key-deps dirty-sub)
                   (::direct-deps dirty-sub))]
    (if key-deps
      (swap! *cache (fn [cache]
                      (-> cache
                          (evict [::dirty sub-id])
                          (miss sub-id (assoc dirty-sub ::key-deps key-deps)))))
      (let [v (calc-cache-entry context sub-id)]
        (swap! *cache (fn [cache]
                        (-> cache
                            (evict [::dirty sub-id])
                            (miss sub-id v))))))))

(defn- add-dep [deps k v]
  (if (= ::context deps)
    ::context
    (assoc deps k v)))

(defn- register-cache-entry [context *cache cache sub-id]
  (if (has? cache sub-id)
    (swap! *cache hit sub-id)
    (let [recalc #(swap! *cache miss sub-id (calc-cache-entry context sub-id))]
      (if (has? cache [::dirty sub-id])
        (if (*processing-dirty* sub-id)
          (do (swap! *cache evict [::dirty sub-id])
              (recalc))
          (binding [*processing-dirty* (conj *processing-dirty* sub-id)]
            (sub-from-dirty context *cache cache sub-id)))
        (recalc)))))

(defn sub [context k & args]
  (let [sub-id (apply vector k args)
        {::keys [*cache *direct-deps *key-deps]} context
        cache @*cache
        ret (if (fn? k)
              (let [entry (-> (register-cache-entry context *cache cache sub-id)
                              (lookup sub-id))]
                (when *key-deps
                  (swap! *key-deps set/union (::key-deps entry)))
                (::value entry))
              (do
                (when (seq args)
                  (throw (ex-info "Subscribing to keys does not allow additional args"
                                  {:k k :args args})))
                (when *key-deps
                  (swap! *key-deps conj k))
                (get-in context [::m k])))]
    (when *direct-deps
      (assert-not-leaked cache (::parent-sub-id context) :child-sub-id sub-id)
      (swap! *direct-deps add-dep sub-id ret))
    ret))

(defn- intersects? [s1 s2]
  (if (< (count s2) (count s1))
    (recur s2 s1)
    (some #(contains? s2 %) s1)))

(defn- invalidate-cache [cache old-m new-m]
  (let [changed-keys (into #{}
                           (comp (mapcat keys)
                                 (distinct)
                                 (remove #(= (old-m %) (new-m %))))
                           [old-m new-m])
        changed-sub-ids (into #{} (map vector) changed-keys)]
    (reduce (fn [acc [k v]]
              (let [direct-deps (::direct-deps v)]
                (cond
                  (= ::context direct-deps)
                  (evict acc k)

                  (intersects? changed-sub-ids (set (keys direct-deps)))
                  (evict acc k)

                  (intersects? changed-keys (::key-deps v))
                  (-> acc
                      (evict k)
                      (miss [::dirty k] v))

                  :else
                  acc)))
            cache
            cache)))

(defn reset [context new-m]
  (let [{::keys [m *cache *direct-deps]} context
        cache @*cache]
    (when *direct-deps
      (assert-not-leaked cache (::parent-sub-id context))
      (reset! *direct-deps ::context))
    {::m new-m
     ::*cache (atom (invalidate-cache cache m new-m))}))

(defn swap [context f & args]
  (reset context (apply f (::m context) args)))

(defn create [m cache-factory]
  {::m m
   ::*cache (atom (->cache (cache-factory {})))})

(defn clear-cache! [context]
  (swap! (::*cache context) #(reduce evict % (keys %))))
