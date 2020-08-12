(ns e04-state-with-context
  (:require [cljfx.api :as fx]
            [clojure.core.cache :as cache]))

(def *state
  (atom (fx/create-context
          {:first-name "Vlad"
           :last-name "Protsenko"}
          cache/lru-cache-factory)))

(defn text-input [{:keys [fx/context label-text key]}]
  {:fx/type :v-box
   :children [{:fx/type :label
               :text label-text}
              {:fx/type :text-field
               :on-text-changed #(swap! *state fx/swap-context assoc key %)
               :text (fx/sub-val context get key)}]})

(defn root [{:keys [fx/context]}]
  (let [first-name (fx/sub-val context :first-name)
        last-name (fx/sub-val context :last-name)]
    {:fx/type :stage
     :showing true
     :scene {:fx/type :scene
             :root {:fx/type :v-box
                    :children [{:fx/type :label
                                :text (str "You are " first-name " " last-name "!")}
                               {:fx/type text-input
                                :label-text "First Name"
                                :key :first-name}
                               {:fx/type text-input
                                :label-text "Last Name"
                                :key :last-name}]}}}))

(def renderer
  (fx/create-renderer
    :middleware (comp
                  fx/wrap-context-desc
                  (fx/wrap-map-desc (fn [_]
                                      {:fx/type root})))
    :opts {:fx.opt/type->lifecycle #(or (fx/keyword->lifecycle %)
                                        (fx/fn->lifecycle-with-context %))}))

(fx/mount-renderer *state renderer)
