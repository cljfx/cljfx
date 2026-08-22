(ns build
  (:require [cemerick.pomegranate.aether :as aether]
            [clojure.tools.build.api :as b]))

(def lib 'cljfx/cljfx)
(def version (format "1.10.%s" (b/git-count-revs nil)))
(def target-dir "target")
(def pom-file (str target-dir "/pom.xml"))
(def artifacts
  [{:src-dir "src"
    :class-dir (str target-dir "/classes")
    :jar-file (format "%s/cljfx-%s.jar" target-dir version)}
   {:src-dir "jdk8"
    :class-dir (str target-dir "/classes-jdk8")
    :classifier "jdk8"
    :jar-file (format "%s/cljfx-%s-jdk8.jar" target-dir version)}
   {:src-dir "jdk11"
    :class-dir (str target-dir "/classes-jdk11")
    :classifier "jdk11"
    :jar-file (format "%s/cljfx-%s-jdk11.jar" target-dir version)}])
(def pom-basis
  (delay
    (b/create-basis
      {:root nil
       :project nil
       :extra {:deps {'org.clojure/clojure {:mvn/version "1.11.2"}}}})))

(defn clean [_]
  (b/delete {:path target-dir}))

(defn print-version [_]
  (println version))

(defn- pom-opts [destination]
  (merge {:basis @pom-basis
          :lib lib
          :version version
          :src-pom "pom.xml"
          :scm {:url "https://github.com/cljfx/cljfx"}}
         destination))

(defn jar [_]
  (clean nil)
  (b/write-pom (pom-opts {:target target-dir}))
  (doseq [{:keys [src-dir class-dir jar-file classifier]} artifacts]
    (b/copy-dir {:src-dirs [src-dir]
                 :target-dir class-dir})
    (when-not classifier
      (b/write-pom (pom-opts {:class-dir class-dir})))
    (b/jar {:class-dir class-dir
            :jar-file jar-file}))
  (println "Built" version)
  {:version version
   :pom-file pom-file
   :jar-files (mapv :jar-file artifacts)})

(defn- artifact-map []
  (into {[:extension "pom"] pom-file}
        (map (fn [{:keys [classifier jar-file]}]
               [(cond-> [:extension "jar"]
                  classifier (conj :classifier classifier))
                jar-file]))
        artifacts))

(defn release [_]
  (let [token (System/getenv "CLOJARS_TOKEN")]
    (when-not (seq token)
      (throw (ex-info "CLOJARS_TOKEN is not set" {})))
    (jar nil)
    (println "Deploying" (str lib) version "to Clojars as vlaaad")
    (System/setProperty "aether.checksums.forSignature" "true")
    (System/setProperty "aether.checksums.omitChecksumsForExtensions" "")
    (aether/deploy
      :coordinates [lib version]
      :artifact-map (artifact-map)
      :repository {"clojars" {:url "https://clojars.org/repo"
                              :username "vlaaad"
                              :password token}}
      :transfer-listener :stdout)
    (println "Released" version)
    {:version version}))
