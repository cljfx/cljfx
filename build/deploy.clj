(ns deploy
  (:require [cemerick.pomegranate.aether :as aether]
            [clojure.java.io :as io]
            [clojure.data.xml :as xml]))

(def default-repo-settings {"clojars" {:url "https://clojars.org/repo"
                                       :username (System/getenv "CLOJARS_USERNAME")
                                       :password (System/getenv "CLOJARS_PASSWORD")}})

(def artifact-id-tag :xmlns.http%3A%2F%2Fmaven.apache.org%2FPOM%2F4.0.0/artifactId)
(def group-id-tag :xmlns.http%3A%2F%2Fmaven.apache.org%2FPOM%2F4.0.0/groupId)
(def version-tag :xmlns.http%3A%2F%2Fmaven.apache.org%2FPOM%2F4.0.0/version)

;; copied directly from leiningen
(defn- extension [f]
  (if-let [[_ signed-extension] (re-find #"\.([a-z]+\.asc)$" f)]
    signed-extension
    (if (= "pom.xml" (.getName (io/file f)))
      "pom"
      (last (.split f "\\.")))))

(defn classifier
  "The classifier is be located between the version and extension name of the artifact.
  See http://maven.apache.org/plugins/maven-deploy-plugin/examples/deploying-with-classifiers.html "
  [version f]
  (let [pattern (re-pattern (format "%s-(\\p{Alnum}*)\\.%s" version (extension f)))
        [_ classifier-of] (re-find pattern f)]
    (when (seq classifier-of)
      classifier-of)))
;; copy stops here

(defn coordinates-from-pom [pom-str]
  (let [tmp (->> pom-str
                 xml/parse-str
                 :content
                 (remove string?)
                 (keep (fn [{:keys [tag] :as m}]
                         (when (or (= tag artifact-id-tag)
                                   (= tag group-id-tag)
                                   (= tag version-tag))
                           {(keyword (name tag)) (first (:content m))})))
                 (apply merge))]
    {:coordinates [(symbol (str (:groupId tmp) "/" (:artifactId tmp))) (:version tmp)]}))

(defn artifacts [version files]
  (into {} (for [f files]
             [[:extension (extension f)
               :classifier (classifier version f)] f])))

(defn all-artifacts [version files]
  (artifacts version (into ["pom.xml"] files)))

(defmulti deploy :installer)

(defmethod deploy "deploy" [{:keys [artifact-map coordinates repository]
                             :or {repository default-repo-settings}}]
  (println "Deploying" (str (first coordinates) "-" (second coordinates)) "to clojars as"
           (-> repository vals first :username))
  (aether/deploy :artifact-map artifact-map
                 :repository repository
                 :coordinates coordinates))

(defmethod deploy "install" [{:keys [artifact-map coordinates]}]
  (println "Installing" (str (first coordinates) "-" (second coordinates)) "to your local `.m2`")
  (aether/install :artifact-map artifact-map
                  :transfer-listener :stdout
                  :coordinates coordinates)
  (println "done."))

(defn -main [deploy-or-install & files]
  (let [coordinates (coordinates-from-pom (slurp "pom.xml"))]
    (->> {:installer deploy-or-install
          :artifact-map (all-artifacts (second (:coordinates coordinates)) files)}
         (merge coordinates)
         deploy)))

