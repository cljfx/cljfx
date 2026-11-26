(ns cljfx.fx.pagination
  "Part of a public API"
  (:require [cljfx.coerce :as coerce]
            [cljfx.component :as component]
            [cljfx.composite :as composite]
            [cljfx.fx.control :as fx.control]
            [cljfx.fx.stack-pane :as fx.stack-pane]
            [cljfx.lifecycle :as lifecycle]
            [cljfx.platform :as platform])
  (:import [javafx.beans.value ChangeListener]
           [javafx.scene AccessibleRole Node]
           [javafx.scene.control Pagination]
           [javafx.util Callback]))

(set! *warn-on-reflection* true)

(defn- create-page [desc opts pages]
  (let [page (lifecycle/create lifecycle/root {:fx/type fx.stack-pane/lifecycle
                                              :children [desc]} opts)
        ^Node node (component/instance page)
        parent (.parentProperty node)]
    (letfn [(dispose-if-detached! []
              (when (nil? (.getParent node))
                (when-let [dispose! (get @pages node)]
                  (vswap! pages dissoc node)
                  (dispose!))))]
      (let [listener (reify ChangeListener
                       (changed [_ _ _ new-parent]
                         (when (nil? new-parent)
                           (platform/run-later (dispose-if-detached!)))))]
        (.addListener parent listener)
        (vswap! pages assoc node
                (fn dispose-page! []
                  (.removeListener parent listener)
                  (lifecycle/delete lifecycle/root page opts)))
        ;; The skin can probe for a page without attaching the returned node.
        (platform/run-later (dispose-if-detached!))
        node))))

(def ^:private page-factory
  (reify lifecycle/Lifecycle
    (create [_ factory opts]
      (let [pages (volatile! {})
            callback
            (cond
              (instance? Callback factory)
              factory

              (fn? factory)
              (reify Callback
                (call [_ index]
                  (when @pages
                    (let [desc (factory index)]
                      (when (some? desc)
                        (create-page desc opts pages))))))

              :else
              (coerce/fail Callback factory))]
        (with-meta {:factory factory :opts opts :callback callback :pages pages}
                   {`component/instance :callback})))
    (advance [this component factory opts]
      (if (and (= factory (:factory component))
               (= opts (:opts component)))
        component
        (let [new-component (lifecycle/create this factory opts)]
          (lifecycle/delete this component opts)
          new-component)))
    (delete [_ {:keys [pages]} _]
      (let [disposers @pages]
        (vreset! pages nil)
        (run! (fn dispose-page! [f] (f)) (vals disposers))))))

(def props
  (merge
    fx.control/props
    (composite/props Pagination
      ;; overrides
      :style-class [:list lifecycle/scalar :coerce coerce/style-class :default "pagination"]
      :accessible-role [:setter lifecycle/scalar :coerce (coerce/enum AccessibleRole)
                        :default :pagination]
      ;; definitions
      :current-page-index [:setter lifecycle/scalar :coerce int :default 0]
      :on-current-page-index-changed [:property-change-listener lifecycle/change-listener]
      :max-page-indicator-count [:setter lifecycle/scalar :coerce int :default 10]
      :page-count [:setter lifecycle/scalar :coerce int :default Integer/MAX_VALUE]
      :page-factory [:setter page-factory])))

(def lifecycle
  (lifecycle/annotate
    (composite/describe Pagination
      :ctor []
      :props props)
    :pagination))
