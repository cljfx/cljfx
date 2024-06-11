(ns e45-git-ui
  (:require [cljfx.api :as fx]
            [cljfx.ext.list-view :as fx.ext.list-view]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [clojure.string :as str])
  (:import [java.time Instant ZoneId]
           [java.time.format DateTimeFormatter]
           [javafx.event ActionEvent]
           [javafx.scene Node]
           [javafx.stage DirectoryChooser]))

;; git log ui: shows changes and diffs for a selected directory.
;; uses ext-effect and ext-state for the state management.

(defn- sh! [opts & args]
  (let [{:keys [exit out err] :as ret} (apply sh/sh (if (map? opts)
                                                      (into (vec args) cat opts)
                                                      (into [opts] args)))]
    (if (zero? exit)
      (str/trim-newline out)
      (throw (ex-info (str "\"" (str/join " " args) "\" failed with exit code " exit
                           (when-not (str/blank? err)
                             (str "\n" err)))
                      ret)))))

(def ^DateTimeFormatter ui-formatter
  (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm"))

(def ^DateTimeFormatter git-iso-formatter
  (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss xxxx"))

(defn- parse-date-time [s]
  (Instant/from (.parse git-iso-formatter s)))

(defn- split-lines [s]
  (if (str/blank? s) [] (str/split-lines s)))

(defn- log! [dir]
  (let [unstaged (into (split-lines (sh! {:dir dir} "git" "diff" "--name-only"))
                       (split-lines (sh! {:dir dir} "git" "ls-files" "--others" "--exclude-standard")))
        staged (split-lines (sh! {:dir dir} "git" "diff" "--staged" "--name-only"))]
    (-> []
        (cond->
          (seq unstaged) (conj {:type :unstaged :files unstaged})
          (seq staged) (conj {:type :staged :files staged}))

        (into
          (map #(-> (zipmap [:sha :message :author :date] (.split ^String % "\0"))
                    (update :date parse-date-time)
                    (assoc :type :commit)))
          (split-lines
            (sh! {:dir dir} "git" "log" "--pretty=format:%H%x00%s%x00%an%x00%ad" "--date=iso"))))))

(defn- check-if-inside-work-tree! [dir]
  (try
    (sh! {:dir dir} "git" "rev-parse" "--is-inside-work-tree")
    true
    (catch Exception _ false)))

(defmacro async [& body]
  `(let [f# (future
              (try ~@body
                   (catch InterruptedException ~'e (throw ~'e))
                   (catch Exception ~'e
                     (tap> ~'e)
                     (throw ~'e))))]
     #(future-cancel f#)))

(defn- parse-diff [sh-out]
  (into []
        (comp
          (drop-while #(not= \@ (first %)))
          (drop 1)
          (map (fn [s]
                 [(case (first s)
                    \space :unchanged
                    \- :removed
                    \+ :added
                    \\ :comment)
                  (subs s 1)])))
        (split-lines sh-out)))

(defn- load-file-diff! [dir sha diff selected-file swap-parent-state]
  (when-not diff
    (async
      ;; here, swap parent state is a state of the log view
      (let [diff (parse-diff (sh! {:dir dir} "git" "diff" "--ignore-space-change" "--ignore-cr-at-eol" "--unified=99999" (str sha "^") sha "--" selected-file))]
        (swap-parent-state assoc-in [:details sha :diff selected-file] diff)))))

(defn- load-uncommitted-file-diff! [dir type diff selected-file swap-parent-state]
  (when-not diff
    (async
      ;; here, swap parent state is a root state of the UI (because we need to reset it on focus)
      (swap-parent-state
        assoc-in
        [:diff type selected-file]
        (parse-diff
          (case type
            :staged (sh! {:dir dir} "git" "diff" "--staged" "--ignore-space-change" "--ignore-cr-at-eol" "--unified=99999" "--" selected-file)
            :unstaged (sh! {:dir dir} "git" "diff" "--ignore-space-change" "--ignore-cr-at-eol" "--unified=99999" "--" selected-file)))))))


(defn- files-with-diff-view [{:keys [state swap-state files diff]}]
  {:fx/type :h-box
   :spacing 10
   :children
   [{:fx/type fx.ext.list-view/with-selection-props
     :props {:selected-index state
             :on-selected-index-changed #(swap-state (constantly %))}
     :desc {:fx/type :list-view
            :min-width 250
            :max-width 250
            :cell-factory {:fx/cell-type :list-cell
                           :describe (fn [file]
                                       {:text file})}
            :items files}}
    (if diff
      {:fx/type :list-view
       :h-box/hgrow :always
       :cell-factory {:fx/cell-type :list-cell
                      :describe (fn [[type line :as e]]
                                  (if e
                                    {:style {:-fx-background-color (case type
                                                                     :unchanged "#fff"
                                                                     :removed "#fbb"
                                                                     :added "#bfb"
                                                                     :comment "#bbb")
                                             :-fx-font-family "monospace"
                                             :-fx-text-fill :black}
                                     :text line}
                                    {}))}
       :items diff}
      {:fx/type :label
       :wrap-text true
       :text "Loading..."})]})

(defn- commit-files-impl-view [{:keys [dir sha detail state swap-state swap-parent-state]}]
  (let [{:keys [files diff]} detail
        selected-file (files state)
        diff (get diff selected-file)]
    {:fx/type fx/ext-effect
     :args [dir sha diff selected-file swap-parent-state]
     :fn load-file-diff!
     :desc {:fx/type files-with-diff-view
            :state state
            :swap-state swap-state
            :files files
            :diff diff}}))

(defn commit-files-view [{:keys [sha dir detail swap-state]}]
  (let [{:keys [files]} detail]
    (if (seq files)
      {:fx/type fx/ext-recreate-on-key-changed
       :key sha
       :desc {:fx/type fx/ext-state
              :initial-state 0
              :desc {:fx/type commit-files-impl-view
                     :dir dir
                     :detail detail
                     :sha sha
                     :swap-parent-state swap-state}}}
      {:fx/type :region})))

(defn uncommited-files-impl-view [{:keys [dir entry diff state swap-state swap-parent-state]}]
  (let [{:keys [type files]} entry
        selected-file (files state)
        diff (get diff selected-file)]
    {:fx/type fx/ext-effect
     :args [dir type diff selected-file swap-parent-state]
     :fn load-uncommitted-file-diff!
     :desc {:fx/type files-with-diff-view
            :state state
            :swap-state swap-state
            :files files
            :diff diff}}))

(defn uncommitted-files-view [{:keys [parent-state entry swap-parent-state]}]
  (let [{:keys [dir diff]} parent-state]
    {:fx/type fx/ext-recreate-on-key-changed
     :key (:type entry)
     :desc {:fx/type fx/ext-state
            :initial-state 0
            :desc {:fx/type uncommited-files-impl-view
                   :dir dir
                   :entry entry
                   :diff (get diff (:type entry))
                   :swap-parent-state swap-parent-state}}}))

(defn- load-diff! [dir sha]
  {:message (sh! {:dir dir} "git" "log" "--format=%B" "-n" "1" sha)
   :files (split-lines (sh! {:dir dir} "git" "show" "--name-only" "--pretty=format:" sha))})

(defn- load-commit-details! [dir maybe-sha loaded swap-state]
  (when (and maybe-sha (not loaded))
    (async
      (let [details (load-diff! dir maybe-sha)]
        (swap-state assoc-in [:details maybe-sha] details)))))

;; here, state is a local state of the log view.

(defn- log-impl-view [{:keys [state swap-state parent-state swap-parent-state]}]
  (let [{:keys [log dir]} parent-state
        {:keys [index details]} state
        selected-entry (log index)
        maybe-sha (:sha selected-entry)
        maybe-detail (details maybe-sha)]
    {:fx/type fx/ext-effect
     :args [dir maybe-sha maybe-detail swap-state]
     :fn load-commit-details!
     :desc {:fx/type :h-box
            :spacing 10
            :children
            [{:fx/type fx.ext.list-view/with-selection-props
              :props {:selected-index index
                      :on-selected-index-changed #(swap-state assoc :index %)}
              :desc
              {:fx/type :list-view
               :max-width 250
               :min-width 250
               :cell-factory
               {:fx/cell-type :list-cell
                :describe
                (fn [e]
                  (if e
                    (case (:type e)
                      :unstaged
                      {:text "Unstaged changes"
                       :style {:-fx-text-fill :red}}

                      :staged
                      {:text "Staged changes"
                       :style {:-fx-text-fill :green}}

                      :commit
                      {:graphic {:fx/type :h-box
                                 :max-width (- 250 30)
                                 :spacing 10
                                 :children
                                 [{:fx/type :label
                                   :h-box/hgrow :always
                                   :text (:message e)
                                   :min-width 0
                                   :max-width ##Inf}
                                  {:fx/type :text
                                   :text (.format ui-formatter (.atZone ^Instant (:date e) (ZoneId/systemDefault)))}]}})
                    {}))}
               :items log}}
             (case (:type selected-entry)
               (:unstaged :staged)
               {:fx/type uncommitted-files-view
                :h-box/hgrow :always
                :entry selected-entry
                :parent-state parent-state
                :swap-parent-state swap-parent-state}

               :commit
               {:fx/type :v-box
                :spacing 10
                :h-box/hgrow :always
                :children [{:fx/type :grid-pane
                            :hgap 10
                            :column-constraints [{:fx/type :column-constraints
                                                  :min-width :use-pref-size}]
                            :children [{:fx/type :label
                                        :grid-pane/row 0
                                        :grid-pane/column 0
                                        :grid-pane/halignment :right
                                        :grid-pane/valignment :top
                                        :text "Commit SHA"}
                                       {:fx/type :label
                                        :grid-pane/row 0
                                        :grid-pane/column 1
                                        :grid-pane/valignment :top
                                        :text (:sha selected-entry)}
                                       {:fx/type :label
                                        :grid-pane/row 1
                                        :grid-pane/column 0
                                        :grid-pane/halignment :right
                                        :grid-pane/valignment :top
                                        :text "Author"}
                                       {:fx/type :label
                                        :grid-pane/row 1
                                        :grid-pane/column 1
                                        :text (:author selected-entry)}
                                       {:fx/type :label
                                        :grid-pane/row 2
                                        :grid-pane/column 0
                                        :grid-pane/halignment :right
                                        :grid-pane/valignment :top
                                        :text "Date"}
                                       {:fx/type :label
                                        :grid-pane/row 2
                                        :grid-pane/column 1
                                        :text (.format ui-formatter (.atZone ^Instant (:date selected-entry) (ZoneId/systemDefault)))}
                                       {:fx/type :label
                                        :grid-pane/row 3
                                        :grid-pane/column 0
                                        :grid-pane/halignment :right
                                        :grid-pane/valignment :top
                                        :text "Message"}
                                       {:fx/type :label
                                        :grid-pane/row 3
                                        :grid-pane/column 1
                                        :wrap-text true
                                        :text (or (:message maybe-detail)
                                                  (:message selected-entry))}]}
                           (if maybe-detail
                             {:fx/type commit-files-view
                              :v-box/vgrow :always
                              :sha maybe-sha
                              :detail maybe-detail
                              :dir dir
                              :swap-state swap-state}
                             {:fx/type :label
                              :text "Loading..."})]})]}}))


(defn- log-view [{:keys [state swap-state]}]
  (let [{:keys [dir]} state]
    {:fx/type fx/ext-recreate-on-key-changed
     :key dir
     :desc {:fx/type fx/ext-state
            :initial-state {:index 0 :details {}}
            :desc {:fx/type log-impl-view
                   :parent-state state
                   :swap-parent-state swap-state}}}))

(defn- load-log! [dir swap-state]
  (async
    (if (check-if-inside-work-tree! dir)
      (let [log (log! dir)]
        (swap-state #(-> % (assoc :dir dir :log log) (dissoc :invalid :diff))))
      (swap-state #(-> % (assoc :dir dir :invalid true) (dissoc :log :diff))))))

(defn- reload-log! [dir swap-state]
  (async
    (if (check-if-inside-work-tree! dir)
      (let [log (log! dir)]
        (swap-state #(cond-> % (= dir (:dir %)) (-> (assoc :log log) (dissoc :invalid :diff)))))
      (swap-state #(cond-> % (= dir (:dir %)) (-> (assoc :invalid true) (dissoc :log :diff)))))))

(defn- root-view [{:keys [state swap-state]}]
  (let [{:keys [dir invalid log]} state]
    {:fx/type fx/ext-effect
     :args [dir swap-state]
     :fn load-log!
     :desc {:fx/type :stage
            :width 1400
            :height 768
            :showing true
            :on-focused-changed #(when % (reload-log! dir swap-state))
            :scene
            {:fx/type :scene
             :root
             {:fx/type :v-box
              :padding 20
              :spacing 10
              :children
              [{:fx/type :h-box
                :spacing 10
                :alignment :center-left
                :children
                [{:fx/type :label
                  :h-box/hgrow :always
                  :max-width ##Inf
                  :text dir}
                 {:fx/type :button
                  :text "Select directory"
                  :on-action (fn [^ActionEvent e]
                               (when-let [dir (.showDialog
                                                (doto (DirectoryChooser.)
                                                  (.setInitialDirectory (io/file dir))
                                                  (.setTitle "Select Git Project Directory"))
                                                (.getWindow (.getScene ^Node (.getTarget e))))]
                                 (swap-state #(-> %
                                                  (assoc :dir (.getCanonicalPath dir))
                                                  (dissoc :invalid :log :diff)))))}]}
               (cond
                 invalid
                 {:fx/type :label
                  :text "Selected directory is not a git working tree!"}

                 (and log (seq log))
                 {:fx/type log-view
                  :v-box/vgrow :always
                  :state state
                  :swap-state swap-state}

                 log
                 {:fx/type :label :text "Empty repo!"}

                 :else
                 {:fx/type :label :text "Loading..."})]}}}}))

(fx/on-fx-thread
  (fx/create-component
    {:fx/type fx/ext-state
     :initial-state {:dir (.getCanonicalPath (io/file "."))}
     :desc {:fx/type root-view}}))
