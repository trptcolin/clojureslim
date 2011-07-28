(ns clojureslim.statement-executor
  (:require [clojureslim.text-transformations :as tt]
            [clojure.string :as string])
  (:import [fitnesse.slim StatementExecutor]))

(def instances (atom {}))
(def slim-variables (atom {}))

(def exception-tag "__EXCEPTION__:")

(defn assign-slim-variable [variable value]
  (swap! slim-variables assoc variable value))

(defn replace-slim-variables [args]
  (map (fn [arg]
         (str (get @slim-variables
                (string/replace arg #"^\$" "")
                arg)))
       args))

(defn find-function
  ([name]
    (last ; the var itself
      (first ; TODO: handle collisions
              (filter (fn [[k v]] (= k (symbol name)))
                      (mapcat ns-publics (all-ns))))))
  ; TODO: pick smartly - same ns as the constructor
  ([name instance]
   (find-function name)))

(def unreported-exception-function-names
  #{"table" "begin-table" "end-table" "reset" "execute"})

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

    ; TODO: handle actual arg-passing
    ; TODO: handle wrong # of args to constructor and other reflection problems
    (create [instance-name fixture-name args]
      (let [replaced-fixture-name (first (replace-slim-variables [fixture-name]))]
        (if-let [fixture-generator (find-function (tt/dasherize replaced-fixture-name))]
            (if-let [instance (apply fixture-generator args)]
              (do (swap! instances assoc instance-name instance)
                "OK")
              (do (print-str exception-tag "Problem creating" replaced-fixture-name)
                  ))
            (do (print-str exception-tag "Couldn't find fixture for" replaced-fixture-name)
                ))))

    ; public abstract Object getInstance(String instanceName);
    ; TODO: implement. who calls this anyway?
    (getInstance [instance-name]
      (println "getInstance called"))

    (call [instance-name method-name args]
      (let [replaced-args (replace-slim-variables args)]
        (try
          (let [instance (@instances instance-name)
                method (find-function method-name instance)]
              (apply method instance replaced-args))
          (catch Throwable e
            (if (unreported-exception-function-names method-name)
              nil
              (print-str exception-tag
                         "problem with"
                         method-name
                         (pr-str (first replaced-args))
                         (tt/pr-str-stack-trace e)))))))

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
      (println "callAndAssign called with variable" variable
               ", instance-name" instance-name
               ", method-name" method-name
               ", args" args)
      (let [result (.call this instance-name method-name args)]
        (assign-slim-variable variable result)
        result
        ))



))


