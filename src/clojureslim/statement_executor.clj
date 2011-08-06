(ns clojureslim.statement-executor
  (:require [clojureslim.text-transformations :as tt]
            [clojureslim.variables :as variables]
            [clojure.string :as string])
  (:import [fitnesse.slim StatementExecutor]))

(def instances (atom {}))
(defn set-instance [instance-name value]
  (swap! instances assoc instance-name value))

(def slim-variables (atom {}))

(def exception-tag "__EXCEPTION__:")

(defn assign-slim-variable [variable value]
  (swap! slim-variables assoc variable value))

(defn replace-slim-variables [args]
  (variables/replace-slim-variables @slim-variables args))

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
        "OK"
        (catch Exception e
          (str exception-tag " " e))))

    (create [instance-name fixture-name args]
      (try
        (let [fixture-name-or-instance (first (replace-slim-variables [fixture-name]))
              args (replace-slim-variables args)]
          (cond (not (string? fixture-name-or-instance))
                  (do (swap! instances assoc instance-name fixture-name-or-instance)
                    "OK")
                (re-seq #"^library" instance-name)
                  ; include library globally
                  (do
                    (.addPath this fixture-name))
                  ; call-function
                (re-seq #"/" fixture-name)
                  (let [[fixture-ns fixture-fn] (string/split fixture-name #"/")]
                    (prn fixture-ns fixture-fn)
                    (apply
                      (ns-resolve (symbol fixture-ns) (symbol fixture-fn))
                      args))
                :default
                  (let [replaced-fixture-name (tt/dasherize fixture-name-or-instance)
                        _ (prn replaced-fixture-name)]
                    (if-let [fixture-generator (find-function replaced-fixture-name)]
                      (let [instance (apply fixture-generator args)]
                        (swap! instances assoc instance-name instance)
                        "OK")
                      (do
                        (print-str
                          exception-tag
                          (format "message:<<COULD_NOT_INVOKE_CONSTRUCTOR %s[%d]>>"
                                  replaced-fixture-name
                                  (count args))))))))
        (catch Throwable e
          (print-str exception-tag
                     "Problem creating fixture for" fixture-name
                     "\n" (tt/pr-str-stack-trace e)))))

    ; public abstract Object getInstance(String instanceName);
    ; TODO: implement. who calls this anyway?
    (getInstance [instance-name]
      (println "getInstance called: " instance-name)
      (let [instance (get @instances instance-name)]
        (prn instance)
        instance))

    ; TODO: make this smarter - use same ns as the instance
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
                         (format "message:<<NO_METHOD_IN_CLASS %s[%d] %s>>"
                                 method-name
                                 (count args)
                                 instance-name)
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
               ", args" (seq args))
      (let [result (.call this instance-name method-name args)]
        (assign-slim-variable variable result)
        result))))

