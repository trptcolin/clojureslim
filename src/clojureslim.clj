(ns clojureslim
  (:require [clojureslim.executor :as executor]
            [chee.string :as cs])
  (:import [fitnesse.slim NameTranslator
                          SlimFactory
                          SlimService]))

(deftype ClojureNameTranslator []
  NameTranslator
  (translate [_ name] (cs/spear-case name)))

(def translator (ClojureNameTranslator.))

(defn ^SlimFactory clojure-slim-factory []
  (proxy [SlimFactory] []
    (getMethodNameTranslator [] translator)
    (getStatementExecutor [] (executor/new-executor))))

(defn -main [& args]
  (let [options (SlimService/parseCommandLine (into-array String args))
        slim-factory (clojure-slim-factory)]
    (println "clojureslim working directory: " (System/getProperty "user.dir"))
    (SlimService/startWithFactory slim-factory options)
    0))