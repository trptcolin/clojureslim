(defproject clojureslim "0.0.1-SNAPSHOT"
  :description "The SliM (Simple List Invocation Method) protocol implented for Clojure"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.fitnesse/fitnesse "20110721"]]
  :dev-dependencies [[speclj "1.4.0"]]
  :aot [clojureslim]
  :test-path "spec")
