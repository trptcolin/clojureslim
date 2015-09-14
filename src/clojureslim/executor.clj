(ns clojureslim.executor
  (:require [chee.string :as cs]
            [clj-stacktrace.repl :as st]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojureslim.protocol :as protocol]
            [clojureslim.variables :as variables])
  (:import [fitnesse.slim StatementExecutor StatementExecutorInterface SlimException SlimServer SlimError]
           (clojure.lang IReference)))

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
            (let [[fixture-ns fixture-fn] (str/split fixture-name #"/")]
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

(defn unload-ns [ns-sym]
  (remove-ns ns-sym)
  (dosync (alter @#'clojure.core/*loaded-libs* set/difference #{ns-sym})))

(defn add-path2 [state path]
  (when-not (some #(= path %) (:paths @state))
    (swap! state update-in [:paths] conj path)))

(defn existing-ns [ns-tail path]
  (let [ns-name (str path "." ns-tail)
        ns-sym (symbol ns-name)]
    (if (find-ns ns-sym)
      ns-sym
      (try
        (require [ns-sym])
        ns-sym
        (catch java.io.FileNotFoundException _ nil)))))

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
  (swap! state assoc-in [:instances id] instance))

(defn create-fixture2 [state id fixture-name args]
  (let [ns-tail (cs/spear-case fixture-name)
        ns (first (filter identity (map #(existing-ns ns-tail %) (:paths @state))))]
    (if ns
      (let [instance (instantiate ns args)]
        (when-not (instance? IReference instance)
          (throw (SlimException. "Fixture constructors must return an atom/ref")))
        (alter-meta! instance assoc :ns ns)
        (store-fixture state id instance))
      (throw (SlimException. (format "%s[%d]" ns-tail (count args)) SlimServer/NO_CONSTRUCTOR)))))

(defn- resolve-fixture-method [instance method-name]
  (prn "method-name instance (meta instance): " method-name instance (meta instance))
  (when instance
    (if-let [ns (:ns (meta instance))]
      (do
        (prn "(ns-resolve ns (symbol method-name)): " (ns-resolve ns (symbol method-name)))
        (ns-resolve ns (symbol method-name)))
      (throw (SlimError. (format "message:<<ns missing on fixture instance>>"))))))



;(defn do-call [state id method-name args]
;  (try
;    (let [fn-name (cs/spear-case method-name)]
;      (if-let [instance (get (:instances @state) id)]
;        (if-let [ns (:ns (meta instance))]
;          (if-let [f (ns-resolve ns (symbol fn-name))]
;            (apply f (cons instance args))
;            (throw (SlimError. (format "message:<<%s %s[%d] %s.>>" SlimServer/NO_METHOD_IN_CLASS fn-name (inc (count args)) (name ns)))))
;          (throw (SlimError. (format "message:<<%s %s. Should be impossible!>>" SlimServer/NO_CLASS id))))
;        (throw (SlimError. (format "message:<<%s %s.>>" SlimServer/NO_INSTANCE id)))))
;    (catch Throwable e
;      (st/pst e)
;      (SlimException. e))))

()

(defn find-method [state instance method-name]
  (let [fixtures (cons instance (:libraries @state))]
    (first (remove nil?
                   (for [fixture fixtures]
                     (when-let [method (resolve-fixture-method fixture method-name)]
                       [fixture method]))))))

(defn do-call [state id method-name args]
  (try
    (let [fn-name (cs/spear-case method-name)
          instance (get (:instances @state) id)]
      (if-let [[fixture method] (find-method state instance method-name)]
        (apply method (cons fixture args))
        (if instance
          (throw (SlimError. (format "message:<<%s %s[%d] %s.>>" SlimServer/NO_METHOD_IN_CLASS fn-name (inc (count args)) (name (:ns (meta instance))))))
          (throw (SlimError. (format "message:<<%s %s.>>" SlimServer/NO_INSTANCE id))))))
    (catch Throwable e
      (st/pst e)
      (SlimException. ^Throwable e))))


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

  (addPath [_ path] (add-path2 state path))
  (create [_ id fixture-name args] (create-fixture2 state id fixture-name args))
  ;
  ;Object callAndAssign(String var1, String var2, String var3, Object... var4) throws SlimException;
  ;
  ;Object call(String var1, String var2, Object... var3) throws SlimException;
  (call [_ id method-name args] (do-call state id method-name args))
  )

(def executor-state
  {:paths           []
   :instances       {}
   :libraries       []
   :stop-requested? false})

(defn new-executor []
  (ClojureStatementExecutor. (atom executor-state))
  )



