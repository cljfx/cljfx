(ns e14-charts
  (:require [cljfx.api :as fx]))

(def pie-chart
  {:fx/type :pie-chart
   :title "Browser Market Share"
   :data [{:fx/type :pie-chart-data :name "Chrome" :pie-value 64}
          {:fx/type :pie-chart-data :name "Internet Explorer" :pie-value 10}
          {:fx/type :pie-chart-data :name "Firefox" :pie-value 9}
          {:fx/type :pie-chart-data :name "Edge" :pie-value 4}
          {:fx/type :pie-chart-data :name "Safari" :pie-value 3}
          {:fx/type :pie-chart-data :name "Other" :pie-value 10}]})

(def area-chart
  {:fx/type :area-chart
   :title "Imaginary temperature comparisons"
   :x-axis {:fx/type :category-axis}
   :y-axis {:fx/type :number-axis}
   :data [{:fx/type :xy-chart-series
           :name "How it should be"
           :data [{:fx/type :xy-chart-data :x-value "Winter" :y-value -2}
                  {:fx/type :xy-chart-data :x-value "Spring" :y-value 10}
                  {:fx/type :xy-chart-data :x-value "Summer" :y-value 22}
                  {:fx/type :xy-chart-data :x-value "Autumn" :y-value 10}]}
          {:fx/type :xy-chart-series
           :name "How it feels like it is"
           :data [{:fx/type :xy-chart-data :x-value "Winter" :y-value -20}
                  {:fx/type :xy-chart-data :x-value "Spring" :y-value 2}
                  {:fx/type :xy-chart-data :x-value "Summer" :y-value 32}
                  {:fx/type :xy-chart-data :x-value "Autumn" :y-value 2}]}]})

(def bar-chart
  {:fx/type :bar-chart
   :title "Top headline phrases"
   :legend-visible false
   :x-axis {:fx/type :number-axis}
   :y-axis {:fx/type :category-axis}
   :data [{:fx/type :xy-chart-series
           :data [{:fx/type :xy-chart-data :x-value 8961 :y-value "will make you"}
                  {:fx/type :xy-chart-data :x-value 4099 :y-value "this is why"}
                  {:fx/type :xy-chart-data :x-value 3199 :y-value "can we guess"}
                  {:fx/type :xy-chart-data :x-value 2398 :y-value "only X in"}
                  {:fx/type :xy-chart-data :x-value 1610 :y-value "the reason is"}
                  {:fx/type :xy-chart-data :x-value 1560 :y-value "are freaking out"}
                  {:fx/type :xy-chart-data :x-value 1425 :y-value "X stunning photos"}
                  {:fx/type :xy-chart-data :x-value 1388 :y-value "tears of joy"}
                  {:fx/type :xy-chart-data :x-value 1337 :y-value "is what happens"}
                  {:fx/type :xy-chart-data :x-value 1287 :y-value "make you cry"}]}]})

(def bubble-chart
  {:fx/type :bubble-chart
   :title "Hours Spent Online by Age and Gender"
   :x-axis {:fx/type :number-axis
            :lower-bound 0
            :upper-bound 10
            :auto-ranging false
            :label "Age"}
   :y-axis {:fx/type :number-axis
            :lower-bound 0
            :upper-bound 10
            :auto-ranging false
            :label "Hours spent online"}
   :data [{:fx/type :xy-chart-series
           :name "Female"
           :data [{:fx/type :xy-chart-data :x-value 1 :y-value 1}
                  {:fx/type :xy-chart-data :x-value 3 :y-value 3}
                  {:fx/type :xy-chart-data :x-value 5 :y-value 7}]}
          {:fx/type :xy-chart-series
           :name "Male"
           :data [{:fx/type :xy-chart-data :x-value 1 :y-value 1}
                  {:fx/type :xy-chart-data :x-value 3 :y-value 4}
                  {:fx/type :xy-chart-data :x-value 5 :y-value 6}]}]})

(def line-chart
  {:fx/type :line-chart
   :x-axis {:fx/type :number-axis
            :auto-ranging false}
   :y-axis {:fx/type :number-axis
            :auto-ranging false}
   :data [{:fx/type :xy-chart-series
           :name "x²"
           :data (for [i (range 100)]
                   {:fx/type :xy-chart-data :x-value i :y-value (* i i)})}
          {:fx/type :xy-chart-series
           :name "sin(x)"
           :data (for [i (range 100)]
                   {:fx/type :xy-chart-data
                    :x-value i
                    :y-value (+ 20 (Math/sin i))})}
          {:fx/type :xy-chart-series
           :name "log(x)"
           :data (for [i (range 100)]
                   {:fx/type :xy-chart-data
                    :x-value i
                    :y-value (Math/log i)})}
          {:fx/type :xy-chart-series
           :name "sqrt(x)"
           :data (for [i (range 100)]
                   {:fx/type :xy-chart-data
                    :x-value i
                    :y-value (Math/sqrt i)})}]})

(def scatter-chart
  {:fx/type :scatter-chart
   :x-axis {:fx/type :number-axis :label "Weight"}
   :y-axis {:fx/type :number-axis :label "Height"}
   :data [{:fx/type :xy-chart-series
           :name "Boys"
           :data [{:fx/type :xy-chart-data :x-value 2.6 :y-value 47.1}
                  {:fx/type :xy-chart-data :x-value 5.3 :y-value 59.7}
                  {:fx/type :xy-chart-data :x-value 6.7 :y-value 64.7}
                  {:fx/type :xy-chart-data :x-value 7.4 :y-value 68.2}
                  {:fx/type :xy-chart-data :x-value 8.4 :y-value 73.9}
                  {:fx/type :xy-chart-data :x-value 10.1 :y-value 81.6}]}
          {:fx/type :xy-chart-series
           :name "Girls"
           :data [{:fx/type :xy-chart-data :x-value 2.6 :y-value 46.7}
                  {:fx/type :xy-chart-data :x-value 5.0 :y-value 58.4}
                  {:fx/type :xy-chart-data :x-value 6.2 :y-value 63.7}
                  {:fx/type :xy-chart-data :x-value 6.9 :y-value 67}
                  {:fx/type :xy-chart-data :x-value 7.8 :y-value 72.5}
                  {:fx/type :xy-chart-data :x-value 9.6 :y-value 80.1}]}]})

(def stacked-area-chart
  {:fx/type :stacked-area-chart
   :title "Imaginary website analytics"
   :x-axis {:fx/type :number-axis
            :auto-ranging false
            :lower-bound 2016
            :upper-bound 2018}
   :y-axis {:fx/type :number-axis}
   :data [{:fx/type :xy-chart-series
           :name "New visitors"
           :data [{:fx/type :xy-chart-data :x-value 2016 :y-value 100}
                  {:fx/type :xy-chart-data :x-value 2017 :y-value 120}
                  {:fx/type :xy-chart-data :x-value 2018 :y-value 150}]}
          {:fx/type :xy-chart-series
           :name "Returning visitors"
           :data [{:fx/type :xy-chart-data :x-value 2016 :y-value 10}
                  {:fx/type :xy-chart-data :x-value 2017 :y-value 5}
                  {:fx/type :xy-chart-data :x-value 2018 :y-value 10}]}]})

(def stacked-bar-chart
  {:fx/type :stacked-bar-chart
   :title "Male Age Structure"
   :x-axis {:fx/type :number-axis}
   :y-axis {:fx/type :category-axis}
   :data [{:fx/type :xy-chart-series
           :name "0-14 years"
           :data [{:fx/type :xy-chart-data :y-value "United Kingdom" :x-value 6}
                  {:fx/type :xy-chart-data :y-value "Germany" :x-value 7}
                  {:fx/type :xy-chart-data :y-value "Mexico" :x-value 16}
                  {:fx/type :xy-chart-data :y-value "Japan" :x-value 9}
                  {:fx/type :xy-chart-data :y-value "Russia" :x-value 12}
                  {:fx/type :xy-chart-data :y-value "Brazil" :x-value 25}
                  {:fx/type :xy-chart-data :y-value "United States" :x-value 29}]}
          {:fx/type :xy-chart-series
           :name "15-64 years"
           :data [{:fx/type :xy-chart-data :y-value "United Kingdom" :x-value 19}
                  {:fx/type :xy-chart-data :y-value "Germany" :x-value 29}
                  {:fx/type :xy-chart-data :y-value "Mexico" :x-value 30}
                  {:fx/type :xy-chart-data :y-value "Japan" :x-value 45}
                  {:fx/type :xy-chart-data :y-value "Russia" :x-value 50}
                  {:fx/type :xy-chart-data :y-value "Brazil" :x-value 55}
                  {:fx/type :xy-chart-data :y-value "United States" :x-value 89}]}
          {:fx/type :xy-chart-series
           :name "65+ years"
           :data [{:fx/type :xy-chart-data :y-value "United Kingdom" :x-value 4}
                  {:fx/type :xy-chart-data :y-value "Germany" :x-value 6}
                  {:fx/type :xy-chart-data :y-value "Mexico" :x-value 2}
                  {:fx/type :xy-chart-data :y-value "Japan" :x-value 9}
                  {:fx/type :xy-chart-data :y-value "Russia" :x-value 7}
                  {:fx/type :xy-chart-data :y-value "Brazil" :x-value 4}
                  {:fx/type :xy-chart-data :y-value "United States" :x-value 15}]}]})

(fx/on-fx-thread
  (fx/create-component
    {:fx/type :stage
     :showing true
     :width 960
     :height 540
     :title "Chart Examples"
     :scene {:fx/type :scene
             :root {:fx/type :tab-pane
                    :tabs [{:fx/type :tab
                            :text "Pie Chart"
                            :closable false
                            :content pie-chart}
                           {:fx/type :tab
                            :text "Area Chart"
                            :closable false
                            :content area-chart}
                           {:fx/type :tab
                            :text "Bar Chart"
                            :closable false
                            :content bar-chart}
                           {:fx/type :tab
                            :text "Bubble Chart"
                            :closable false
                            :content bubble-chart}
                           {:fx/type :tab
                            :text "Line Chart"
                            :closable false
                            :content line-chart}
                           {:fx/type :tab
                            :text "Scatter Chart"
                            :closable false
                            :content scatter-chart}
                           {:fx/type :tab
                            :text "Stacked Area Chart"
                            :closable false
                            :content stacked-area-chart}
                           {:fx/type :tab
                            :text "Stacked Bar Chart"
                            :closable false
                            :content stacked-bar-chart}]}}}))
