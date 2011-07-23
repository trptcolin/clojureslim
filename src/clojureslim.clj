(ns clojureslim
  (:require [clojure.string :as string])
  (:import [fitnesse.slim JavaSlimFactory
                          NameTranslator
                          SlimFactory
                          SlimServer
                          SlimService
                          StatementExecutor]))

(defn make-method-name-translator []
  (proxy [NameTranslator] []
    (translate [s] s)))

(defn underscorize [s]
  (.replaceAll s "-" "_"))

(defn get-record-class [name package]
  (try (Class/forName (string/join "." [package name]))
    (catch ClassNotFoundException e (println name "not found in" package))))

(defn find-record-class [name]
  (let [nses         (all-ns)
        underscored-nses (map (comp underscorize str) (all-ns))]
    (some (partial get-record-class name)
                   underscored-nses)))

(def instances (atom {}))

(defn make-statement-executor []
  (proxy [StatementExecutor] []

    ; public abstract void setVariable(String name, Object value);
    ;   TODO: postponing for now because its only effects are changing behavior of other methods
    (setVariable [name value]
      (println "setVariable called"))

    (addPath [path]
      (try
        (require (symbol path))
        (catch Exception e (println e) (throw e)))
      "OK")


    ; public abstract Object create(String instanceName, String className, Object[] args);
    ; make
    ;  register in the instance map - keyed on instanceName, which Fitnesse decides
    ; TODO: handle actual arg-passing
    ; TODO: handle wrong # of args to constructor and other reflection problems
    (create [instance-name class-name args]
      (let [record-class (find-record-class class-name)]
        (if record-class
          (if-let [instance (.newInstance record-class)]
            (do (swap! instances assoc instance-name instance)
                "OK")
            (throw (ClassNotFoundException.)))
          (throw (ClassNotFoundException.)))
        ))

    ; public abstract Object getInstance(String instanceName);
    ; TODO: implement. who calls this anyway?
    (getInstance [instance-name]
      (println "getInstance called"))

    ; public abstract Object call(String instanceName, String methodName, Object... args);
    (call [instance-name method-name args]
      (let [instance (@instances instance-name)
            instance-class (.getClass instance)
            _ (prn instance-class)
            methods (.getDeclaredMethods instance-class)
            method (first (filter #(= method-name (.getName %)) methods))
            ]
        (.invoke method instance args)))


    ; public abstract boolean stopHasBeenRequested();
    ; TODO: this definitely won't do in the long run
    (stopHasBeenRequested []
      false)

    ; public abstract void reset();
    ; TODO: implement
    (reset []
      (println "reset called"))

    ; public abstract Object callAndAssign(String variable, String instanceName, String methodName,
    ;     Object[] args);
    ; store slim variables
    ; TODO: store these
    ; TODO: replace method calls with the values that are stored
    (callAndAssign [variable instance-name method-name args]
      (println "callAndAssign called"))

))

(defn make-clojure-slim-factory [method-name-translator statement-executor]
  (proxy [SlimFactory] []
    (getMethodNameTranslator []
      method-name-translator)
    (getStatementExecutor []
      statement-executor)))

(defn main [& args]
  (let [port (Integer/parseInt (first args))
        method-name-translator (make-method-name-translator)
        statement-executor (make-statement-executor)
        slim-factory (make-clojure-slim-factory
                       method-name-translator
                       statement-executor)
        verbose true
        slim-server (.getSlimServer slim-factory verbose)]
    (SlimService. port slim-server)))

