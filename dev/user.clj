(ns user
  (:require [clojure.string :as str]
            [clojure.reflect :as reflect]))

(defn infer-props [inspected-class]
  (let [instance (try (.newInstance inspected-class)
                      (catch Exception _
                        nil))
        members (:members (reflect/reflect inspected-class))]
    (->> members
         (filter #(-> % :flags :public))
         (keep
           (fn [member]
             (let [name-parts (-> member :name name (str/split #"(?=\p{Upper})") rest)
                   kw-name (keyword (str/join "-" (map str/lower-case name-parts)))]
               (cond
                 (and
                   (-> member :name name (str/starts-with? "set"))
                   (-> member :parameter-types count (= 1)))
                 (let [default-value (try (.invoke (.getMethod
                                                     ^Class inspected-class
                                                     (str "get" (str/join name-parts))
                                                     (into-array Class []))
                                                   instance
                                                   (into-array Object []))
                                          (catch Exception _
                                            (try
                                              (.invoke (.getMethod
                                                         ^Class inspected-class
                                                         (str "is" (str/join name-parts))
                                                         (into-array Class []))
                                                       instance
                                                       (into-array Object []))
                                              (catch Exception _
                                                '???))))
                       parameter-type-sym (first (:parameter-types member))]
                   [kw-name
                    (vec
                      (concat
                        [:setter]
                        (case parameter-type-sym
                          double ['prop/scalar :coerce 'double]
                          boolean ['prop/scalar]
                          java.lang.Object ['prop/scalar]
                          java.lang.String ['prop/scalar]
                          javafx.scene.Node ['prop/component]
                          javafx.scene.paint.Color ['prop/scalar :coerce 'coerce/color]
                          javafx.event.EventHandler ['prop/scalar
                                                     :coerce 'coerce/event-handler]
                          java.lang.Runnable ['prop/scalar :coerce 'coerce/runnable]
                          javafx.geometry.Point3D ['prop/scalar :coerce 'coerce/point-3d]
                          javafx.geometry.Rectangle2D ['prop/scalar
                                                       :coerce 'coerce/rectangle-2d]
                          javafx.scene.paint.Paint ['prop/scalar :coerce 'coerce/paint]
                          javafx.scene.image.Image ['prop/scalar :coerce 'coerce/image]
                          int ['prop/scalar :coerce 'int]
                          (cond
                            (and (class? (resolve parameter-type-sym))
                                 (.isEnum ^Class (resolve parameter-type-sym)))
                            ['prop/scalar :coerce (list 'coerce/enum
                                                        (-> parameter-type-sym
                                                            name
                                                            (str/split #"\.")
                                                            last
                                                            symbol))]
                            :else [parameter-type-sym]))
                        (when-not (nil? default-value)
                          [:default
                           (cond
                             (instance? Enum default-value)
                             (-> default-value
                                 .name
                                 (str/split #"_")
                                 (->> (map str/lower-case)
                                      (str/join "-"))
                                 keyword)

                             :else
                             default-value)])))])

                 (and
                   (-> member :name name (str/starts-with? "get"))
                   (-> member :return-type (= 'javafx.collections.ObservableList)))
                 [kw-name (vec (concat
                                 [:list member]))]))))
         (sort-by first)
         (mapcat identity))))
