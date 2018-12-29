(ns cljfx.event
  (:require [clojure.string :as str])
  (:import [javafx.scene.input KeyEvent MouseEvent MouseDragEvent RotateEvent ScrollEvent
                               SwipeEvent TouchEvent ZoomEvent GestureEvent TouchPoint]
           [javafx.event Event ActionEvent]
           [javafx.scene.media MediaErrorEvent]
           [javafx.stage WindowEvent]))

(set! *warn-on-reflection* true)

(defn- screaming-case->keyword [str]
  (-> str str/lower-case (str/replace "_" "-") keyword))

(defprotocol Datafy
  (datafy [e]
    "Datafy event

    clojure.core.protocols/Datafy is not used here to prevent clashes with other
    libraries datafying java-fx"))

(defn- datafy-event [^Event e]
  {:source (.getSource e)
   :target (.getTarget e)
   :event-type (screaming-case->keyword (.getName (.getEventType e)))
   :consumed (.isConsumed e)})

(defn- datafy-mouse-event [^MouseEvent e]
  (merge
    (datafy-event e)
    {:alt-down (.isAltDown e)
     :button (screaming-case->keyword (.name (.getButton e)))
     :click-count (.getClickCount e)
     :control-down (.isControlDown e)
     :drag-detect (.isDragDetect e)
     :meta-down (.isMetaDown e)
     :middle-button-down (.isMiddleButtonDown e)
     :pick-result (.getPickResult e)
     :popup-trigger (.isPopupTrigger e)
     :primary-button-down (.isPrimaryButtonDown e)
     :scene-x (.getSceneX e)
     :scene-y (.getSceneY e)
     :screen-x (.getScreenX e)
     :screen-y (.getSceneY e)
     :secondary-button-down (.isSecondaryButtonDown e)
     :shift-down (.isShiftDown e)
     :shortcut-down (.isShortcutDown e)
     :still-since-press (.isStillSincePress e)
     :synthesized (.isSynthesized e)
     :x (.getX e)
     :y (.getY e)
     :z (.getZ e)}))

(defn- datafy-gesture-event [^GestureEvent e]
  (merge
    (datafy-event e)
    {:x (.getX e)
     :y (.getY e)
     :z (.getZ e)
     :screen-x (.getScreenX e)
     :screen-y (.getScreenY e)
     :scene-x (.getSceneX e)
     :scene-y (.getSceneY e)
     :shift-down (.isShiftDown e)
     :control-down (.isControlDown e)
     :alt-down (.isAltDown e)
     :meta-down (.isMetaDown e)
     :direct (.isDirect e)
     :inertia (.isInertia e)
     :pick-result (.getPickResult e)}))

(defn datafy-touch-point [^TouchPoint p]
  {:target (.getTarget p)
   :grabbed (.getGrabbed p)
   :id (.getId p)
   :state (screaming-case->keyword (.name (.getState p)))
   :x (.getX p)
   :y (.getY p)
   :z (.getZ p)
   :screen-x (.getScreenX p)
   :screen-y (.getScreenY p)
   :scene-x (.getSceneX p)
   :scene-y (.getSceneY p)
   :pick-result (.getPickResult p)})

(extend-protocol Datafy
  KeyEvent
  (datafy [e]
    (merge
      (datafy-event e)
      {:character (.getCharacter e)
       :text (.getText e)
       :code (.getCode e)
       :shift-down (.isShiftDown e)
       :control-down (.isControlDown e)
       :alt-down (.isAltDown e)
       :meta-down (.isMetaDown e)
       :shortcut-down (.isShortcutDown e)}))

  MouseDragEvent
  (datafy [e]
    (merge
      (datafy-mouse-event e)
      {:gesture-source (.getGestureSource e)}))

  MouseEvent
  (datafy [e]
    (datafy-mouse-event e))

  RotateEvent
  (datafy [e]
    (merge
      (datafy-gesture-event e)
      {:angle (.getAngle e)
       :total-angle (.getTotalAngle e)}))

  ScrollEvent
  (datafy [e]
    (merge
      (datafy-gesture-event e)
      {:delta-x (.getDeltaX e)
       :delta-y (.getDeltaY e)
       :total-delta-x (.getTotalDeltaX e)
       :total-delta-y (.getTotalDeltaY e)
       :text-delta-x-units (screaming-case->keyword (.name (.getTextDeltaXUnits e)))
       :text-delta-y-units (screaming-case->keyword (.name (.getTextDeltaYUnits e)))
       :text-delta-x (.getTextDeltaX e)
       :text-delta-t (.getTextDeltaY e)
       :touch-count (.getTouchCount e)
       :multiplier-x (.getMultiplierX e)
       :multiplier-y (.getMultiplierY e)}))

  SwipeEvent
  (datafy [e]
    (merge
      (datafy-gesture-event e)
      {:touch-count (.getTouchCount e)}))

  TouchEvent
  (datafy [e]
    (merge
      (datafy-event e)
      {:event-set-id (.getEventSetId e)
       :shift-down (.isShiftDown e)
       :control-down (.isControlDown e)
       :alt-down (.isAltDown e)
       :meta-down (.isMetaDown e)
       :touch-point (datafy-touch-point (.getTouchPoint e))
       :touch-points (map datafy-touch-point (.getTouchPoints e))}))

  ZoomEvent
  (datafy [e]
    (merge
      (datafy-gesture-event e)
      {:zoom-factor (.getZoomFactor e)
       :total-zoom-factor (.getTotalZoomFactor e)}))

  MediaErrorEvent
  (datafy [e]
    (let [error (.getMediaError e)]
      (merge (datafy-event e)
             {:error (assoc (Throwable->map error)
                       :error-type (screaming-case->keyword (.name (.getType error))))})))

  ActionEvent
  (datafy [e]
    (datafy-event e))

  WindowEvent
  (datafy [e]
    (datafy-event e))

  Object
  (datafy [e] e))

