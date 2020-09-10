(ns deploy
  (:require [cemerick.pomegranate.aether :as aether]
            [clojure.java.io :as io]
            [clojure.data.xml :as xml]))

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
    [(symbol (str (:groupId tmp) "/" (:artifactId tmp))) (:version tmp)]))

(defn artifacts [version files]
  (into {} (for [f files]
             [[:extension (extension f)
               :classifier (classifier version f)] f])))

(defn -main [username token & files]
  (let [coordinates (coordinates-from-pom (slurp "pom.xml"))]
    (println "Deploying" coordinates "to clojars as" username)
    (aether/deploy
      :coordinates coordinates
      :artifact-map (artifacts (second coordinates) (cons "pom.xml" files))
      :repository {"clojars" {:url "https://clojars.org/repo"
                              :username username
                              :password token}})))

