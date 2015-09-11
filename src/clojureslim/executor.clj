(ns clojureslim.executor
  (:require [chee.string :as cs]
            [clojure.set :as set]
            [clojure.string :as string]
            [clojureslim.protocol :as protocol]
            [clojureslim.variables :as variables])
  (:import [fitnesse.slim StatementExecutor StatementExecutorInterface SlimException SlimServer]))

(defn find-function
  ([slim-id name]
   (last                                                    ; the var itself
     ; TODO: pick smarter - use same ns as the constructor?
     (first                                                 ; TODO: handle collisions
       (filter (fn [[k v]] (= k (symbol name)))
               (mapcat ns-publics (all-ns)))))))

(def instances (atom {}))

(defn set-instance [slim-id value]
  (swap! instances assoc slim-id value))

(defn get-instance [slim-id]
  (get @instances slim-id))

(defmulti create-fixture (fn []))

(defn use-existing-fixture [slim-id fixture-name-or-instance]
  (do
    (set-instance slim-id fixture-name-or-instance)
    (protocol/success)))

(defn maybe-add-library [slim-id library-name]
  (try
    (require (symbol library-name))
    ;(set-instance slim-id {:kind :library :path (symbol path)})
    true
    (catch Throwable e (println "can't add library path, e=" e) nil)))

(defn add-library [slim-id library-name]
  (if (or (maybe-add-library slim-id library-name)
          (maybe-add-library slim-id (cs/spear-case library-name)))
    (protocol/success)
    (protocol/library-error library-name)))

(def paths (atom []))
(defn add-path [path]
  (add-library nil path)
  (swap! paths conj path))

(defn create-fixture [slim-id fixture-name args]
  (try
    (let [fixture-name-or-instance (variables/replace-slim-variables fixture-name)
          args (variables/replace-slim-variables args)]
      (cond (not (string? fixture-name-or-instance))
            (use-existing-fixture slim-id fixture-name-or-instance)

            (re-seq #"^library" slim-id)
            (add-library slim-id fixture-name)

            (re-seq #"/" fixture-name)
            (let [[fixture-ns fixture-fn] (string/split fixture-name #"/")]
              (apply
                (ns-resolve (symbol fixture-ns) (symbol fixture-fn))
                args))

            :default
            (let [replaced-fixture-name (cs/spear-case fixture-name-or-instance)]
              (if-let [fixture-generator (find-function slim-id replaced-fixture-name)]
                (let [instance (apply fixture-generator args)]
                  (set-instance slim-id instance)
                  (protocol/success))
                (protocol/no-constructor-error replaced-fixture-name args)))))
    (catch Throwable e
      (protocol/constructor-error fixture-name args e))))

(defn call-fixture-method [slim-id method-name args]
  (try
    (let [replaced-args (variables/replace-slim-variables args)
          instance (get-instance slim-id)
          method (find-function slim-id method-name)]
      (when method
        (try
          (apply method instance replaced-args)
          (catch Throwable e (protocol/method-invokation-error e)))))
    (catch Throwable e
      (protocol/method-missing-error slim-id method-name args e))))

(defn call-and-assign [variable slim-id method-name args]
  (try
    (let [result (call-fixture-method slim-id method-name args)]
      (variables/assign-slim-variable variable result)
      result)
    (catch Exception e
      (protocol/unexpected-error e))))

(defn make-statement-executor []
  (proxy [StatementExecutor] []

    ; TODO: when does this get called?
    (setVariable [name value])

    (addPath [path]
      (add-path path))

    (create [slim-id fixture-name args]
      (create-fixture slim-id fixture-name args))

    (getInstance [slim-id]
      (get-instance slim-id))

    (call [slim-id method-name args]
      (call-fixture-method slim-id method-name args))

    ; TODO: anything we need to do here?
    (stopHasBeenRequested [] false)

    ; TODO: what is this supposed to do?
    (reset [])

    (callAndAssign [variable slim-id method-name args]
      (call-and-assign variable slim-id method-name args))))

;(defn- unload-ns [ns-sym]
;  (remove-ns ns-sym)
;  (dosync (alter @#'clojure.core/*loaded-libs* set/difference #{ns-sym})))

(defn- load-ns [state path]
  (let [ns-sym (symbol path)]
    (when-not (some #(= ns-sym %) (:nses @state))
      (require [ns-sym :reload-all true])
      (swap! state update-in [:nses] conj ns-sym))))

(defn create-fixture2 [state id fixture-name args]
  (let [ctor-sym (symbol (str "new-" (cs/spear-case fixture-name)))
        ctor (first (filter identity (map #(ns-resolve % ctor-sym) (:nses @state))))]
    (if ctor
      (try
        (let [instance (apply ctor args)]
          (swap! state assoc-in [:instances id] instance))
        (catch Throwable e
          (throw (SlimException. (format "%s:%s[%d]" (name ctor-sym) fixture-name (count args)) e SlimServer/COULD_NOT_INVOKE_CONSTRUCTOR true))))
      (throw (SlimException. (format "%s:%s[%d]" (name ctor-sym) fixture-name (count args)) SlimServer/NO_CONSTRUCTOR)))))

(defn do-call [state id method-name args]
  )

(deftype ClojureStatementExecutor [state]
  StatementExecutorInterface
  ;Object getSymbol(String var1);
  ;
  ;Object getInstance(String var1);
  ;
  (stopHasBeenRequested [_] (boolean (:stop-requested? @state)))
  ;
  ;void reset();
  ;
  ;void setInstance(String var1, Object var2);
  ;void assign(String var1, Object var2);

  (addPath [_ path] (load-ns state path))
  (create [_ id fixture-name args] (create-fixture2 state id fixture-name args))
  ;
  ;Object callAndAssign(String var1, String var2, String var3, Object... var4) throws SlimException;
  ;
  ;Object call(String var1, String var2, Object... var3) throws SlimException;
  (call [_ id method-name args] (do-call state id method-name args))
  )

(def executor-state
  {:nses            []
   :stop-requested? false})

(defn new-executor []
  (ClojureStatementExecutor. (atom executor-state))
  )


