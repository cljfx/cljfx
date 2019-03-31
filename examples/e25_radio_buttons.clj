(ns e25-radio-buttons
  (:require [cljfx.api :as fx]))

(def *option
  (atom :a))

(defn radio-group [{:keys [options value on-action]}]
  {:fx/type fx/ext-let-refs
   :refs {::toggle-group {:fx/type :toggle-group}}
   :desc {:fx/type :h-box
          :padding 20
          :spacing 10
          :children (for [option options]
                      {:fx/type :radio-button
                       :toggle-group {:fx/type fx/ext-get-ref
                                      :ref ::toggle-group}
                       :selected (= option value)
                       :text (str option)
                       :on-action (assoc on-action :option option)})}})

(def renderer
  (fx/create-renderer
    :opts {:fx.opt/map-event-handler
           (fn [e]
             (case (:event/type e)
               ::set-option (reset! *option (:option e))))}
    :middleware (fx/wrap-map-desc
                  (fn [option]
                    {:fx/type :stage
                     :showing true
                     :scene {:fx/type :scene
                             :root {:fx/type radio-group
                                    :options [:a :b :c]
                                    :value option
                                    :on-action {:event/type ::set-option}}}}))))

(fx/mount-renderer *option renderer)
