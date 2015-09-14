(defproject hunt-the-wumpus "1.0.0-SNAPSHOT"
            :description "Example Project using Clojure Slim"
            :dependencies [[org.clojure/clojure "1.7.0"]]


            :plugins [[speclj "3.3.1"]]

            :profiles {:dev {:dependencies [[speclj "3.3.1"]
                                            [clojureslim "1.0.0"]]}}
            :source-paths ["src" "spec"]
            :test-paths ["spec"]
            )
