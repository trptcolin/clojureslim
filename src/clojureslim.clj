(ns clojureslim
  (:require [clojureslim.statement-executor :as statement-executor]
            [clojureslim.text-transformations :as tt])
  (:import [fitnesse.slim NameTranslator
                          SlimFactory
                          SlimService]))

(defn make-method-name-translator []
  (proxy [NameTranslator] []
    (translate [s] (tt/dasherize s))))

(defn make-clojure-slim-factory [method-name-translator statement-executor]
  (proxy [SlimFactory] []
    (getMethodNameTranslator [] method-name-translator)
    (getStatementExecutor [] statement-executor)))

(defn -main [& args]
  (let [method-name-translator (make-method-name-translator)
        statement-executor (statement-executor/make-statement-executor)
        options (SlimService/parseCommandLine (into-array String args))
        slim-factory (make-clojure-slim-factory
                       method-name-translator
                       statement-executor)]
    (prn "(System/getPropery \"user.dir\":"  (System/getProperty "user.dir"))
    (SlimService/startWithFactory slim-factory options)
    0))


fitnesseMain.FitNesseMain