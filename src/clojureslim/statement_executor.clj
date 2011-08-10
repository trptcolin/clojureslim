(ns clojureslim.statement-executor
  (:require [clojureslim.fitnesse-messages :as messages]
            [clojureslim.text-transformations :as tt]
            [clojureslim.variables :as variables]
            [clojure.string :as string])
  (:import [fitnesse.slim StatementExecutor]))

(def ^{:future-ns :variables}
  slim-variables (atom {}))

(defn ^{:future-ns :variables}
  assign-slim-variable [variable value]
  (swap! slim-variables assoc variable value))

(defn ^{:future-ns :variables}
  replace-slim-variables [args]
  (variables/replace-slim-variables @slim-variables args))

(defn find-function
  ([name]
    (last ; the var itself
      ; TODO: pick smarter - use same ns as the constructor?
      (first ; TODO: handle collisions
        (filter (fn [[k v]] (= k (symbol name)))
                (mapcat ns-publics (all-ns))))))
  ([name instance]
   (find-function name)))

(defn ^{:future-ns :statement-executor-impl}
  add-library-path [path]
  (try
    (require (symbol path))
    (messages/success)
    (catch Exception e
      (messages/library-error e))))

(def instances (atom {}))

(defn set-instance [slim-id value]
  (swap! instances assoc slim-id value))

(defn get-instance [slim-id]
  (get @instances slim-id))


(defmulti create-fixture (fn []))

(defn use-existing-fixture [slim-id fixture-name-or-instance]
  (do
    (set-instance slim-id fixture-name-or-instance)
    (messages/success)))

(defn add-library-or-dasherized-library [fixture-name]
  ; TODO: actually, should this used as a package / prefix instead?
  (let [include-attempt (add-library-path fixture-name)]
    (if (not= (messages/success) include-attempt)
      (add-library-path (tt/dasherize fixture-name))
      include-attempt)))

(defn ^{:future-ns :statement-executor-impl}
  create-fixture [slim-id fixture-name args]
  (try
    (let [fixture-name-or-instance (first (replace-slim-variables [fixture-name]))
          args (replace-slim-variables args)]
      (cond (not (string? fixture-name-or-instance))
              (use-existing-fixture slim-id fixture-name-or-instance)

            (re-seq #"^library" slim-id)
              (add-library-or-dasherized-library fixture-name)

            (re-seq #"/" fixture-name)
              (let [[fixture-ns fixture-fn] (string/split fixture-name #"/")]
                (apply
                  (ns-resolve (symbol fixture-ns) (symbol fixture-fn))
                  args))

            :default
              (let [replaced-fixture-name (tt/dasherize fixture-name-or-instance)]
                (if-let [fixture-generator (find-function replaced-fixture-name)]
                  (let [instance (apply fixture-generator args)]
                    (swap! instances assoc slim-id instance)
                    (messages/success))
                  (messages/constructor-error replaced-fixture-name args)))))
    (catch Throwable e
      (messages/unexpected-constructor-error fixture-name e))))

(defn ^{:future-ns :statement-executor-impl}
  call-fixture-method [slim-id method-name args]
  ; TODO: make this smarter - use same ns as the instance?
  (let [replaced-args (replace-slim-variables args)]
    (try
      (let [instance (@instances slim-id)
            method (find-function method-name instance)]
        (apply method instance replaced-args))
      (catch Throwable e
        (messages/method-call-error slim-id method-name args e)))))

(defn make-statement-executor []
  (proxy [StatementExecutor] []

    ; TODO: when does this get called?
    (setVariable [name value])

    (addPath [path]
      (add-library-path path))

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
      (let [result (call-fixture-method slim-id method-name args)]
        (assign-slim-variable variable result)
        result))))

