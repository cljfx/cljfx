(ns cljfx.context
  (:require [clojure.set :as set])
  (:import [clojure.lang IPersistentMap]))

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

(defn- assert-not-leaked [cache sub-id]
  (assert (not (has? cache sub-id))
          (str (first sub-id) " is attempting to subscribe to bound context which already has value

Possible reasons:
- you return lazy seq which uses `cljfx.api/sub` while calculating elements
- you leaked context from subscription function's scope without unbinding it first (call `cljfx.api/unbind-context` on it in that case)")))

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

(declare sub)

(defn- sub-from-dirty [context *cache cache sub-id]
  (let [dirty-sub (lookup cache [::dirty sub-id])
        deps (::direct-deps dirty-sub)
        unbound-context (unbind context)]
    (if (every? #(= (get deps %) (apply sub unbound-context %)) (keys deps))
      (swap! *cache (fn [cache]
                      (-> cache
                          (evict [::dirty sub-id])
                          (miss sub-id dirty-sub))))
      (let [v (calc-cache-entry context sub-id)]
        (swap! *cache (fn [cache]
                        (-> cache
                            (evict [::dirty sub-id])
                            (miss sub-id v))))))))

(defn- add-dep [deps k v]
  (if (= ::context deps)
    ::context
    (assoc deps k v)))

(defn sub [context k & args]
  (let [sub-id (apply vector k args)
        {::keys [*cache *direct-deps *key-deps]} context
        cache @*cache
        ret (if (fn? k)
              (let [entry (-> (cond
                                (has? cache sub-id)
                                (do (swap! *cache hit sub-id)
                                    cache)

                                (has? cache [::dirty sub-id])
                                (sub-from-dirty context *cache cache sub-id)

                                :else
                                (swap! *cache miss sub-id (calc-cache-entry context sub-id)))
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
      (assert-not-leaked cache (::parent-sub-id context))
      (swap! *direct-deps add-dep sub-id ret))
    ret))

(defn- intersects? [s1 s2]
  (if (< (count s2) (count s1))
    (recur s2 s1)
    (some #(contains? s2 %) s1)))

(defn invalidate-cache [cache old-m new-m]
  (let [changed-keys (into #{} (remove #(= (old-m %) (new-m %))) (keys old-m))
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
   ::*cache (atom (cache-factory {}))})

(defn clear-cache! [context]
  (swap! (::*cache context) #(reduce evict % (keys %))))