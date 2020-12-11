(ns cljfx.ext.web-view
  (:require [cljfx.lifecycle :as lifecycle]
            [cljfx.prop :as prop]
            [cljfx.mutator :as mutator]
            [cljfx.coerce :as coerce])
  (:import [javafx.scene.web WebView]
           [javafx.util Callback]))

(defn- coerce-content [x]
  (cond
    (string? x) {:content x :content-type "text/html"}
    (and (:content x) (:content-type x)) x
    :else (coerce/fail `coerce-content x)))

(defn- coerce-callback [x]
  (cond
    (instance? Callback x) x
    (fn? x) (reify Callback
              (call [_ in]
                (x in)))
    :else (coerce/fail Callback x)))

(def with-engine-props
  "Extension lifecycle providing WebEngine-related props of a WebView

  Supported keys:
  - `:desc` (required) - component description of a WebView
  - `:props` (optional) - a map of props:
    - `:content`, either html string or a map with `:content` html string and
      `:content-type` string (e.g. \"text/html\")
    - `:java-script-enabled` - boolean
    - `:on-alert` - event handler
    - `:on-error` - event handler
    - `:on-location-changed` - event handler
    - `:on-resized` - event handler
    - `:on-status-changed` - event handler
    - `:on-title-changed` - event handler
    - `:on-visibility-changed` - event handler
    - `:url` - url string to load
    - `:user-agent` - string
    - `:user-data-directory` - file
    - `:user-style-sheet-location` - a local url string, e.g. \"data:...\",
      \"file:...\" or `jar:...`
    - `:on-prompt` - fn (not event handler!) of PromptData to string
    - `:on-create-popup` - fn (not event handler!) of PopupFeatures to WebEngine
    - `:on-confirm` - fn (not event handler!) of string to boolean"
  (lifecycle/make-ext-with-props lifecycle/dynamic
    {:content (prop/make
                (mutator/setter
                  #(.loadContent (.getEngine ^WebView %1) (:content %2) (:content-type %2)))
                lifecycle/scalar
                :coerce coerce-content)
     :java-script-enabled (prop/make
                            (mutator/setter
                              #(.setJavaScriptEnabled (.getEngine ^WebView %1) %2))
                            lifecycle/scalar)
     :on-alert (prop/make
                 (mutator/setter
                   #(.setOnAlert (.getEngine ^WebView %1) %2))
                 lifecycle/event-handler
                 :coerce coerce/event-handler)
     :on-error (prop/make
                 (mutator/setter
                   #(.setOnError (.getEngine ^WebView %1) %2))
                 lifecycle/event-handler
                 :coerce coerce/event-handler)
     :on-location-changed (prop/make
                            (mutator/property-change-listener
                              #(.locationProperty (.getEngine ^WebView %)))
                            lifecycle/change-listener)
     :on-resized (prop/make
                   (mutator/setter
                     #(.setOnResized (.getEngine ^WebView %1) %2))
                   lifecycle/event-handler
                   :coerce coerce/event-handler)
     :on-status-changed (prop/make
                          (mutator/setter
                            #(.setOnStatusChanged (.getEngine ^WebView %1) %2))
                          lifecycle/event-handler
                          :coerce coerce/event-handler)
     :on-title-changed (prop/make
                         (mutator/property-change-listener
                           #(.titleProperty (.getEngine ^WebView %)))
                         lifecycle/change-listener)
     :on-visibility-changed (prop/make
                              (mutator/setter
                                #(.setOnVisibilityChanged (.getEngine ^WebView %1) %2))
                              lifecycle/event-handler
                              :coerce coerce/event-handler)
     :url (prop/make
            (mutator/setter #(.load (.getEngine ^WebView %1) %2))
            lifecycle/scalar)
     :user-agent (prop/make
                   (mutator/setter #(.setUserAgent (.getEngine ^WebView %1) %2))
                   lifecycle/scalar)
     :user-data-directory (prop/make
                            (mutator/setter #(.setUserDataDirectory (.getEngine ^WebView %1) %2))
                            lifecycle/scalar)
     :user-style-sheet-location (prop/make
                                  (mutator/setter #(.setUserStyleSheetLocation (.getEngine ^WebView %1) %2))
                                  lifecycle/scalar)
     :on-prompt (prop/make
                  (mutator/setter #(.setPromptHandler (.getEngine ^WebView %1) %2))
                  lifecycle/scalar
                  :coerce coerce-callback)
     :on-create-popup (prop/make
                        (mutator/setter #(.setCreatePopupHandler (.getEngine ^WebView %1) %2))
                        lifecycle/scalar
                        :coerce coerce-callback)
     :on-confirm (prop/make
                   (mutator/setter #(.setConfirmHandler (.getEngine ^WebView %1) %2))
                   lifecycle/scalar
                   :coerce coerce-callback)}))