(defproject clojureslim "0.0.2"
  :description "The Slim (Simple List Invocation Method) protocol implented for Clojure"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.fitnesse/fitnesse "20150814"]]

  :plugins [[speclj "3.3.1"]]

  :profiles {:dev {:dependencies [[speclj "3.3.1"]]}}

  :source-paths ["src"]
  :test-paths ["spec"]

  :dev-resources-paths ["examples"]
  )
