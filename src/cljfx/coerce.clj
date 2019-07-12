(ns cljfx.coerce
  "Part of a public API"
  (:require [clojure.string :as str])
  (:import [java.util List]
           [javafx.event EventHandler]
           [javafx.scene Cursor]
           [javafx.scene.image Image]
           [javafx.scene.paint Paint Color LinearGradient ImagePattern CycleMethod
                               RadialGradient Stop]
           [javafx.geometry Point3D Rectangle2D Insets Side]
           [javafx.scene.transform Rotate]
           [javafx.util Duration StringConverter Callback]
           [javafx.scene.text Font FontWeight FontPosture]
           [javafx.scene.shape StrokeType StrokeLineJoin StrokeLineCap]
           [javafx.scene.layout Background BackgroundFill CornerRadii BackgroundImage
                                BackgroundRepeat BackgroundPosition BackgroundSize Border
                                BorderStroke BorderStrokeStyle BorderWidths BorderImage
                                BorderRepeat]
           [javafx.util.converter BigDecimalStringConverter BigIntegerStringConverter
                                  BooleanStringConverter ByteStringConverter
                                  CharacterStringConverter DateTimeStringConverter
                                  DefaultStringConverter DoubleStringConverter
                                  FloatStringConverter
                                  IntegerStringConverter LocalDateStringConverter
                                  LocalDateTimeStringConverter LocalTimeStringConverter
                                  LongStringConverter NumberStringConverter
                                  ShortStringConverter]
           [javafx.scene.input KeyCombination KeyCode]
           [javafx.beans.value ObservableValue ChangeListener]
           [javafx.beans Observable InvalidationListener]
           [java.io InputStream]
           [javafx.collections ListChangeListener]
           [javafx.animation Animation Interpolator PathTransition$OrientationType]))

(set! *warn-on-reflection* true)

(defn- kw->screaming-caps [kw]
  (-> kw
      name
      (str/replace "-" "_")
      str/upper-case))

(defn fail [target x]
  (throw (ex-info "Don't know how to coerce" {:target target :x x})))

(defn enum
  ([enum-type]
   (fn [x] (enum enum-type x)))
  ([enum-type x]
   (cond
     (instance? enum-type x) x
     (keyword? x) (Enum/valueOf enum-type (kw->screaming-caps x))
     :else (fail enum-type x))))

(defn color [x]
  (cond
    (instance? Color x) x
    (keyword? x) (Color/valueOf (name x))
    (string? x) (Color/valueOf ^String x)
    :else (fail Color x)))

(defn- stop [[offset x]]
  (Stop. (double offset)
         (color x)))

(defn- map->linear-gradient [m]
  (LinearGradient. ^double (:start-x m)
                   ^double (:start-y m)
                   ^double (:end-x m)
                   ^double (:end-y m)
                   ^boolean (:proportional m)
                   ^CycleMethod (enum CycleMethod (:cycle-method m))
                   ^List (map stop (:stops m))))

(defn- map->radial-gradient [m]
  (RadialGradient. ^double (:focus-angle m)
                   ^double (:focus-distance m)
                   ^double (:center-x m)
                   ^double (:center-y m)
                   ^double (:radius m)
                   ^boolean (:proportional m)
                   ^CycleMethod (enum CycleMethod (:cycle-method m))
                   ^List (map stop (:stops m))))

(defn image [x]
  (cond
    (instance? Image x) x
    (string? x) (Image. ^String x)
    (map? x) (if (contains? x :url)
               (Image. (:url x)
                       (double (:requested-width x 0))
                       (double (:requested-height x 0))
                       (boolean (:preserve-ratio x))
                       (boolean (:smooth x))
                       (boolean (:background-loading x)))
               (Image. ^InputStream (:is x)
                       (double (:requested-width x 0))
                       (double (:requested-height x 0))
                       (boolean (:preserve-ratio x))
                       (boolean (:smooth x))))
    :else (fail Image x)))

(defn map->image-pattern [m]
  (ImagePattern. (image (:image m))
                 ^double (:x m 0.0)
                 ^double (:y m 0.0)
                 ^double (:width m 1.0)
                 ^double (:height m 1.0)
                 ^boolean (:proportional m true)))

(defn paint [x]
  (cond
    (instance? Paint x) x
    (keyword? x) (Color/valueOf (name x))
    (string? x) (Color/valueOf ^String x)
    (and (vector? x) (= :linear-gradient (first x))) (map->linear-gradient (second x))
    (and (vector? x) (= :radial-gradient (first x))) (map->radial-gradient (second x))
    (and (vector? x) (= :image-pattern (first x))) (map->image-pattern (second x))
    :else (fail Paint x)))

(defn cursor [x]
  (cond
    (instance? Cursor x) x
    (keyword? x) (Cursor/cursor (kw->screaming-caps x))
    (string? x) (Cursor/cursor x)
    :else (fail Cursor x)))

(defn event-handler [x]
  (cond
    (instance? EventHandler x)
    x

    (fn? x)
    (reify EventHandler
      (handle [_ event]
        (x event)))

    :else
    (fail EventHandler x)))

(defn runnable [x]
  (cond
    (fn? x)
    (fn []
      (x nil))

    (instance? Runnable x)
    x

    :else
    (fail Runnable x)))

(defn point-3d [x]
  (cond
    (instance? Point3D x) x
    (map? x) (Point3D. (:x x) (:y x) (:z x))
    (= :zero x) Point3D/ZERO
    (= :x-axis x) Rotate/X_AXIS
    (= :y-axis x) Rotate/Y_AXIS
    (= :z-axis x) Rotate/Z_AXIS
    :else (fail Point3D x)))

(defn- normalize-style-maps [prefix x]
  (cond
    (map? x) (mapcat #(normalize-style-maps (conj prefix (key %)) (val %)) x)
    :else [[prefix x]]))

(defn- stringify-style-value [x]
  (cond
    (keyword? x) (name x)
    (vector? x) (str/join " " (map stringify-style-value x))
    :else (str x)))

(defn- map->style [m]
  (->> m
       (normalize-style-maps [])
       (map (fn [[key-vec value]]
              (str (str/join "-" (map name key-vec))
                   ": "
                   (stringify-style-value value))))
       (str/join "; ")))

(defn style [x]
  (cond
    (string? x) x
    (map? x) (map->style x)
    :else (fail "style" x)))

(defn rectangle-2d [x]
  (cond
    (instance? Rectangle2D x) x
    (map? x) (Rectangle2D. (:min-x x) (:min-y x) (:width x) (:height x))
    :else (fail Rectangle2D x)))

(defn duration [x]
  (cond
    (instance? Duration x) x
    (= :zero x) Duration/ZERO
    (= :one x) Duration/ONE
    (= :indefinite x) Duration/INDEFINITE
    (= :unknown x) Duration/UNKNOWN
    (number? x) (Duration. (double x))
    (string? x) (Duration/valueOf x)
    (vector? x) (case (second x)
                  :ms (Duration/millis (double (first x)))
                  :s (Duration/seconds (double (first x)))
                  :m (Duration/minutes (double (first x)))
                  :h (Duration/hours (double (first x))))
    :else (fail Duration x)))

(defn font [x]
  (cond
    (instance? Font x) x
    (= :default x) (Font/getDefault)
    (number? x) (Font. ^double (double x))
    (string? x) (Font/font ^String x)
    (map? x) (let [{:keys [family weight posture size]} x]
               (cond
                 (and family weight posture size)
                 (Font/font family (enum FontWeight weight) (enum FontPosture posture) (double size))

                 (and family weight size)
                 (Font/font ^String family ^FontWeight (enum FontWeight weight) (double size))

                 (and family posture size)
                 (Font/font ^String family ^FontPosture (enum FontPosture posture) (double size))

                 (and family size)
                 (Font/font family (double size))

                 family
                 (Font/font ^String family)))
    :else (fail Font x)))

(defn- corner-radii [x]
  (cond
    (instance? CornerRadii x) x
    (= :empty x) CornerRadii/EMPTY
    (number? x) (CornerRadii. (double x))
    (map? x) (let [{:keys [radius
                           as-percent
                           top-left
                           top-right
                           bottom-right
                           bottom-left]} x]
               (CornerRadii. (double (or top-left radius))
                             (double (or top-right radius))
                             (double (or bottom-right radius))
                             (double (or bottom-left radius))
                             (boolean as-percent)))
    :else (fail CornerRadii x)))

(defn insets [x]
  (cond
    (instance? Insets x) x
    (= :empty x) Insets/EMPTY
    (number? x) (Insets. (double x))
    (map? x) (Insets. (double (:top x 0))
                      (double (:right x 0))
                      (double (:bottom x 0))
                      (double (:left x 0)))
    :else (fail Insets x)))

(defn- background-fill [x]
  (cond
    (instance? BackgroundFill x) x
    (map? x) (BackgroundFill. (some-> x :fill paint)
                              (some-> x :radii corner-radii)
                              (some-> x :insets insets))
    :else (fail BackgroundFill x)))

(def ^:private background-repeat
  (enum BackgroundRepeat))

(def ^:private side
  (enum Side))

(defn- background-position [x]
  (cond
    (instance? BackgroundPosition x) x
    (= :center x) BackgroundPosition/CENTER
    (= :default x) BackgroundPosition/DEFAULT
    (map? x) (BackgroundPosition. (some-> x :horizontal :side side)
                                  (-> x :horizontal :position double)
                                  (-> x :horizontal :as-percentage boolean)
                                  (some-> x :vertical :side side)
                                  (-> x :vertical :position double)
                                  (-> x :vertical :as-percentage boolean))
    :else (fail BackgroundPosition x)))

(defn- background-size [x]
  (cond
    (instance? BackgroundSize x) x
    (= :auto x) BackgroundSize/AUTO
    (= :default x) BackgroundSize/DEFAULT
    (map? x) (BackgroundSize. (-> x :width double)
                              (-> x :height double)
                              (-> x :width-as-percentage boolean)
                              (-> x :height-as-percentage boolean)
                              (-> x :contain boolean)
                              (-> x :cover boolean))
    :else (fail BackgroundSize x)))

(defn- background-image [x]
  (cond
    (instance? BackgroundImage x) x
    (string? x) (BackgroundImage. (image x) nil nil nil nil)
    (map? x) (BackgroundImage. (-> x :image image)
                               (some-> x :repeat-x background-repeat)
                               (some-> x :repeat-y background-repeat)
                               (some-> x :position background-position)
                               (some-> x :size background-size))
    :else (fail BackgroundImage x)))

(defn background [x]
  (cond
    (instance? Background x) x
    (map? x) (Background. ^List (map background-fill (:fills x))
                          ^List (map background-image (:images x)))
    :else (fail Background x)))

(def stroke-type
  (enum StrokeType))

(def stroke-line-join
  (enum StrokeLineJoin))

(def stroke-line-cap
  (enum StrokeLineCap))

(defn- border-stroke-style [x]
  (cond
    (instance? BorderStrokeStyle x) x
    (= :dashed x) BorderStrokeStyle/DASHED
    (= :dotted x) BorderStrokeStyle/DOTTED
    (= :none x) BorderStrokeStyle/NONE
    (= :solid x) BorderStrokeStyle/SOLID
    (map? x) (BorderStrokeStyle. (some-> x :type stroke-type)
                                 (some-> x :line-join stroke-line-join)
                                 (some-> x :line-cap stroke-line-cap)
                                 (double (:miter-limit x 10))
                                 (double (:dash-offset x 0))
                                 (map double (:dash-array x)))
    :else (fail BorderStrokeStyle x)))

(defn- border-widths [x]
  (cond
    (instance? BorderWidths x) x
    (= :auto x) BorderWidths/AUTO
    (= :default x) BorderWidths/DEFAULT
    (= :empty x) BorderWidths/EMPTY
    (= :full x) BorderWidths/FULL
    (number? x) (BorderWidths. (double x))
    (map? x) (let [{:keys [top
                           right
                           bottom
                           left
                           as-percentage
                           top-as-percentage
                           right-as-percentage
                           bottom-as-percentage
                           left-as-percentage]} x]
               (if (or (some? as-percentage)
                       (some? top-as-percentage))
                 (BorderWidths. (double top)
                                (double right)
                                (double bottom)
                                (double left)
                                (boolean (if (some? top-as-percentage)
                                           top-as-percentage
                                           as-percentage))
                                (boolean (if (some? right-as-percentage)
                                           right-as-percentage
                                           as-percentage))
                                (boolean (if (some? bottom-as-percentage)
                                           bottom-as-percentage
                                           as-percentage))
                                (boolean (if (some? left-as-percentage)
                                           left-as-percentage
                                           as-percentage)))
                 (BorderWidths. (double top)
                                (double right)
                                (double bottom)
                                (double left))))
    :else (fail BorderWidths x)))

(def border-repeat
  (enum BorderRepeat))

(defn- border-stroke [x]
  (cond
    (instance? BorderStroke x) x
    (map? x) (BorderStroke. (some-> x :stroke paint)
                            (some-> x :style border-stroke-style)
                            (some-> x :radii corner-radii)
                            (some-> x :widths border-widths)
                            (some-> x :insets insets))
    :else (fail BorderStroke x)))

(defn- border-image [x]
  (cond
    (instance? BorderImage x) x
    (map? x) (BorderImage. (-> x :image image)
                           (some-> x :widths border-widths)
                           (some-> x :insets insets)
                           (some-> x :slices border-widths)
                           (-> x :filled boolean)
                           (some-> x :repeat-x border-repeat)
                           (some-> x :repeat-y border-repeat))
    :else (fail BorderImage x)))

(defn border [x]
  (cond
    (instance? Border x) x
    (= :empty x) Border/EMPTY
    (map? x) (Border. ^List (map border-stroke (:strokes x))
                      ^List (map border-image (:images x)))
    :else (fail Border x)))

(defn string-converter [x]
  (cond
    (instance? StringConverter x)
    x

    :else (case x
            :big-decimal (BigDecimalStringConverter.)
            :big-integer (BigIntegerStringConverter.)
            :boolean (BooleanStringConverter.)
            :byte (ByteStringConverter.)
            :character (CharacterStringConverter.)
            :date-time (DateTimeStringConverter.)
            :default (DefaultStringConverter.)
            :double (DoubleStringConverter.)
            :float (FloatStringConverter.)
            ;; :format (FormatStringConverter. ???) ;; needs format
            :integer (IntegerStringConverter.)
            :local-date (LocalDateStringConverter.)
            :local-date-time (LocalDateTimeStringConverter.)
            :local-time (LocalTimeStringConverter.)
            :long (LongStringConverter.)
            ;; :number-axis-default (NumberAxis$DefaultFormatter. ???) ;; needs axis
            :number (NumberStringConverter.)
            :short (ShortStringConverter.)
            (fail StringConverter x))))

(defn key-combination [x]
  (cond
    (instance? KeyCombination x)
    x

    (string? x)
    (KeyCombination/valueOf x)

    (vector? x)
    (let [modifiers (->> x
                         butlast
                         (map #(-> % name (str/replace "-" " ") str/upper-case)))
          code (last x)
          code-str (cond
                     (instance? KeyCode code) (.getName ^KeyCode code)
                     (keyword? code) (.getName ^KeyCode (enum KeyCode code))
                     (string? code) (str "'" code "'"))]
      (KeyCombination/valueOf (str/join "+" (concat modifiers [code-str]))))

    :else
    (fail KeyCombination x)))

(defn cell-factory [x]
  (cond
    (instance? Callback x) x
    :else (fail Callback x)))

(defn constant-observable-value [x]
  (reify ObservableValue
    (^void addListener [_ ^ChangeListener _])
    (^void removeListener [_ ^ChangeListener _])
    (getValue [_] x)
    Observable
    (^void addListener [_ ^InvalidationListener _])
    (^void removeListener [_ ^InvalidationListener _])))

(defn change-listener ^ChangeListener [x]
  (cond
    (instance? ChangeListener x)
    x

    (fn? x)
    (reify ChangeListener
      (changed [_ _ _ value]
        (x value)))

    :else
    (fail ChangeListener x)))

(defn list-change-listener ^ListChangeListener [x]
  (cond
    (instance? ListChangeListener x)
    x

    (fn? x)
    (reify ListChangeListener
      (onChanged [_ change]
        (x (vec (.getList change)))))

    :else
    (fail ListChangeListener x)))

(defn style-class [x]
  (if (string? x) [x] x))

(defn pref-or-computed-size-double [x]
  (case x
    :use-pref-size Double/NEGATIVE_INFINITY
    :use-computed-size -1.0
    (double x)))

(defn computed-size-double [x]
  (case x
    :use-computed-size -1.0
    (double x)))

(defn animation [x]
  (cond
    (= :indefinite x) Animation/INDEFINITE
    :else (int x)))

(defn interpolator [x]
  (cond
    (instance? Interpolator x) x
    (vector? x) (condp = (nth x 0)
                  :spline (case (count x)
                            5 (let [[_ x1 y1 x2 y2] x]
                                (Interpolator/SPLINE (double x1)
                                                     (double y1)
                                                     (double x2)
                                                     (double y2)))
                             (fail Interpolator x))
                  :tangent (case (count x)
                             3 (let [[_ t v] x]
                                 (Interpolator/TANGENT (duration t)
                                                       (double v)))
                             5 (let [[_ t1 v1 t2 v2] x]
                                 (Interpolator/TANGENT (duration t1)
                                                       (double v1)
                                                       (duration t2)
                                                       (double v2)))
                             (fail Interpolator x))
                  (fail Interpolator x))
    :else (case x
            :discrete Interpolator/DISCRETE
            :ease-both Interpolator/EASE_BOTH
            :ease-in Interpolator/EASE_IN
            :ease-out Interpolator/EASE_OUT
            :linear Interpolator/LINEAR
            (fail Interpolator x))))

(defn path-transition-orientation [x]
  (cond
    (instance? PathTransition$OrientationType x) x
    :else (case x
            :none PathTransition$OrientationType/NONE
            :orthogonal-to-tanget PathTransition$OrientationType/ORTHOGONAL_TO_TANGENT
            (fail PathTransition$OrientationType x))))
