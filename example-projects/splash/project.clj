(defproject splash "0.1.0-SNAPSHOT"
  :description "Example splash screen for cljfx"
  :java-source-paths ["java-src"]
  :dependencies [[cljfx/cljfx "1.7.13"]]
  :repl-options {:init-ns splash}
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dcljfx.skip-javafx-initialization=true"]}}
  ;; entrypoint for splash screen
  :main splash.Main)
