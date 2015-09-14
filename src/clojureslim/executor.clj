(ns clojureslim.executor
  (:require [chee.string :as cs]
            [clj-stacktrace.repl :as st]
            [clojure.set :as set]
            [clojureslim.variables :as variables]
            [fitnesse.slim.slim-helper-library])
  (:import [fitnesse.slim StatementExecutorInterface SlimException SlimServer SlimError]
           (clojure.lang IReference)
           (java.util List)
           (java.io FileNotFoundException)))

(defn list-or-string [value]
  (cond
    (nil? value) nil
    (instance? IReference value) (list-or-string @value)
    (or (instance? List value) (sequential? value) (associative? value)) (mapv list-or-string value)
    :else (str value)))

(defn resolve-vars [state arg]
  (variables/replace-slim-variables (:variables @state) arg))

(defn resolve-name [state name]
  (->> name
       (resolve-vars state)
       cs/spear-case))

(defn unload-ns [ns-sym]
  (remove-ns ns-sym)
  (dosync (alter @#'clojure.core/*loaded-libs* set/difference #{ns-sym})))

(defn add-path [state path]
  (when-not (some #(= path %) (:paths @state))
    (swap! state update-in [:paths] conj path)))

(defn existing-ns [ns-str]
  (let [ns-sym (symbol ns-str)]
    (if (find-ns ns-sym)
      ns-sym
      (try
        (require [ns-sym])
        ns-sym
        (catch FileNotFoundException _ nil)))))

(defn instantiate [ns args]
  (try
    (let [ctor (or (ns-resolve ns 'new) #(atom {}))]
      (apply ctor args))
    (catch Throwable e
      (st/pst e)
      (throw (SlimException. (format "%s/new[%d]" (name ns) (count args)) e SlimServer/COULD_NOT_INVOKE_CONSTRUCTOR true)))))

(defn store-fixture [state id instance]
  (when (.startsWith id "library")
    (swap! state update-in [:libraries] conj instance))
  (swap! state assoc-in [:instances id] instance)
  instance)

(defn- unhandled-exception [state e]
  (let [ename (.getName (.getClass e))]
    (if (or (.contains ename "StopTest") (.contains ename "StopSuite"))
      (swap! state assoc :stop-requested? true)
      (st/pst e)))
  (throw (SlimException. ^Throwable e)))

(defn- create-and-store-fixture [state id ns args]
  (let [args (resolve-vars state args)
        instance (instantiate ns args)]
    (when-not (instance? IReference instance)
      (throw (SlimException. "Fixture constructors must return an atom/ref")))
    (alter-meta! instance assoc :ns ns)
    (store-fixture state id instance)))

(defn create-fixture [state id fixture-name args]
  (try
    (let [potential-actor (resolve-vars state fixture-name)]
      (if (and potential-actor (not (string? potential-actor)))
        (store-fixture state id potential-actor)
        (let [ns-tail (resolve-name state fixture-name)
              potential-nses (cons ns-tail (map #(str % "." ns-tail) (:paths @state)))
              ns (first (filter identity (map existing-ns potential-nses)))]
          (if ns
            (create-and-store-fixture state id ns args)
            (throw (SlimException. (format "%s[%d]" ns-tail (count args)) SlimServer/NO_CONSTRUCTOR true))))))
    (catch Throwable e (unhandled-exception state e))))

(defn- resolve-fixture-method [instance method-name]
  (when instance
    (if-let [ns (:ns (meta instance))]
      (ns-resolve ns (symbol method-name))
      (throw (SlimError. (format "message:<<ns missing on fixture instance>>"))))))

(defn find-method [state instance method-name]
  (let [fixtures (cons instance (:libraries @state))]
    (first (remove nil?
                   (for [fixture fixtures]
                     (when-let [method (resolve-fixture-method fixture method-name)]
                       [fixture method]))))))

(defn do-call [state id method-name args]
  (try
    (let [fn-name (resolve-name state method-name)
          instance (get (:instances @state) id)]
      (if-let [[fixture method] (find-method state instance fn-name)]
        (let [resolved-args (resolve-vars state args)
              full-args (cons fixture resolved-args)]
          (apply method full-args))
        (if instance
          (throw (SlimError. (format "message:<<%s %s[%d] %s.>>" SlimServer/NO_METHOD_IN_CLASS fn-name (inc (count args)) (name (:ns (meta instance))))))
          (throw (SlimError. (format "message:<<%s %s.>>" SlimServer/NO_INSTANCE id))))))
    (catch Throwable e (unhandled-exception state e))))

(defn do-call-and-assign [state variable id method-name args]
  (let [result (do-call state id method-name args)]
    (swap! state assoc-in [:variables variable] result)
    (list-or-string result)))

(deftype ClojureStatementExecutor [state]
  StatementExecutorInterface
  (getSymbol [_ name] (list-or-string (get-in @state [:variables name])))
  (getInstance [_ id] (get-in @state [:instances id]))
  (stopHasBeenRequested [_] (boolean (:stop-requested? @state)))
  (reset [_] (swap! state assoc :stop-requested? false))
  (setInstance [_ id fixture] (swap! state assoc-in [:instances id] fixture))
  (assign [_ variable value] (swap! state assoc-in [:variables variable] value))
  (addPath [_ path] (add-path state path))
  (create [_ id fixture-name args] (create-fixture state id fixture-name args))
  (call [_ id method-name args] (list-or-string (do-call state id method-name args)))
  (callAndAssign [_ variable id method-name args] (do-call-and-assign state variable id method-name args)))

(def executor-state
  {:paths           []
   :instances       {}
   :libraries       () ;LIFO
   :variables       {}
   :stop-requested? false})

(defn new-executor []
  (let [executor (ClojureStatementExecutor. (atom executor-state))
        helper (create-and-store-fixture (.state executor) "library-slim-helper" 'fitnesse.slim.slim-helper-library [])]
    (.setStatementExecutor @helper executor)
    executor))



