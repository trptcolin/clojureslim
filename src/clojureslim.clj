(ns clojureslim
  (:require [clojureslim.statement-executor :as statement-executor]
            [clojureslim.text-transformations :as tt])
  (:import [fitnesse.slim JavaSlimFactory
                          NameTranslator
                          SlimFactory
                          SlimServer
                          SlimService]))

(defn make-method-name-translator []
  (proxy [NameTranslator] []
    (translate [s] (tt/dasherize s))))

(defn make-clojure-slim-factory [method-name-translator statement-executor]
  (proxy [SlimFactory] []
    (getMethodNameTranslator []
      method-name-translator)
    (getStatementExecutor []
      statement-executor)))

(defn main [& args]
  (let [port (Integer/parseInt (first args))
        method-name-translator (make-method-name-translator)
        statement-executor (statement-executor/make-statement-executor)
        slim-factory (make-clojure-slim-factory
                       method-name-translator
                       statement-executor)
        verbose true
        slim-server (.getSlimServer slim-factory verbose)]
    (SlimService. port slim-server)))

