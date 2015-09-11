(defproject clojureslim "1.0.0"
  :description "The Slim (Simple List Invocation Method) protocol implented for Clojure"

  :dependencies [[chee "2.0.0"]
                 [clj-stacktrace "0.2.8"]
                 [org.clojure/clojure "1.7.0"]
                 [org.fitnesse/fitnesse "20150814"]]

  :plugins [[speclj "3.3.1"]]

  :profiles {:dev {:dependencies [[speclj "3.3.1"]]}}

  :source-paths ["src"]
  :test-paths ["spec"]

  :dev-resources-paths ["examples"]
  )
