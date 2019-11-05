(ns version
  (:require [clojure.data.xml :as xml]
            [clojure.zip :as zip]
            [clojure.java.io :as io]))

(xml/alias-uri 'pom "http://maven.apache.org/POM/4.0.0")

(defn- make-xml-element
  [{:keys [tag attrs] :as node} children]
  (with-meta
    (apply xml/element tag attrs children)
    (meta node)))

(defn- xml-assoc-in [root tag-path el]
  (let [z (zip/zipper xml/element? :content make-xml-element root)]
    (zip/root
      (loop [[tag & more-tags :as tags] tag-path
             parent z
             child (zip/down z)]
        (if child
          (if (= tag (:tag (zip/node child)))
            (if (seq more-tags)
              (recur more-tags child (zip/down child))
              (zip/edit child (constantly (xml/sexp-as-element el))))
            (recur tags parent (zip/right child)))
          parent)))))

(defn -main [& version]
  (with-open [reader (io/reader "pom.xml")]
    (let [xml (-> reader
                  (xml/parse :skip-whitespace true)
                  (xml-assoc-in [::pom/version] [::pom/version version])
                  (xml-assoc-in [::pom/scm ::pom/tag] [::pom/tag version])
                  (xml-assoc-in [::pom/properties ::pom/cljfx.version] [::pom/cljfx.version version]))]
      (with-open [writer (io/writer "pom.xml")]
        (xml/indent xml writer)))))