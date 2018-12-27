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
                          double ['lifecycle/scalar :coerce 'double]
                          boolean ['lifecycle/scalar]
                          java.lang.Object ['lifecycle/scalar]
                          java.lang.String ['lifecycle/scalar]
                          javafx.scene.Node ['lifecycle/dynamic-hiccup]
                          javafx.scene.paint.Color ['lifecycle/scalar :coerce 'coerce/color]
                          javafx.event.EventHandler ['lifecycle/scalar
                                                     :coerce 'coerce/event-handler]
                          java.lang.Runnable ['lifecycle/scalar :coerce 'coerce/runnable]
                          javafx.geometry.Point3D ['lifecycle/scalar :coerce 'coerce/point-3d]
                          javafx.geometry.Rectangle2D ['lifecycle/scalar
                                                       :coerce 'coerce/rectangle-2d]
                          javafx.scene.paint.Paint ['lifecycle/scalar :coerce 'coerce/paint]
                          javafx.scene.image.Image ['lifecycle/scalar :coerce 'coerce/image]
                          int ['lifecycle/scalar :coerce 'int]
                          (cond
                            (and (class? (resolve parameter-type-sym))
                                 (.isEnum ^Class (resolve parameter-type-sym)))
                            ['lifecycle/scalar :coerce (list 'coerce/enum
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
