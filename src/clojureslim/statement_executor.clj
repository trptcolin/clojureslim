(ns clojureslim.statement-executor
  (:require [clojureslim.fitnesse-messages :as messages]
            [clojureslim.text-transformations :as tt]
            [clojureslim.variables :as variables]
            [clojure.string :as string])
  (:import [fitnesse.slim StatementExecutor]))

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
    (messages/success)))

(defn ^{:future-ns :statement-executor-impl}
maybe-add-library [slim-id library-name]
  (try
    (require (symbol library-name))
    ;(set-instance slim-id {:kind :library :path (symbol path)})
    true
    (catch Throwable e (println "can't add library path, e=" e) nil)))

(defn ^{:future-ns :statement-executor-impl}
add-library [slim-id library-name]
  (if (or (maybe-add-library slim-id library-name)
          (maybe-add-library slim-id (tt/dasherize library-name)))
    (messages/success)
    (messages/library-error library-name)))

(def paths (atom []))
(defn add-path [path]
  (add-library nil path)
  (swap! paths conj path))

(defn ^{:future-ns :statement-executor-impl}
create-fixture [slim-id fixture-name args]
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
            (let [replaced-fixture-name (tt/dasherize fixture-name-or-instance)]
              (if-let [fixture-generator (find-function slim-id replaced-fixture-name)]
                (let [instance (apply fixture-generator args)]
                  (set-instance slim-id instance)
                  (messages/success))
                (messages/no-constructor-error replaced-fixture-name args)))))
    (catch Throwable e
      (messages/constructor-error fixture-name args e))))

(defn ^{:future-ns :statement-executor-impl}
call-fixture-method [slim-id method-name args]
  (try
    (let [replaced-args (variables/replace-slim-variables args)
          instance (get-instance slim-id)
          method (find-function slim-id method-name)]
      (apply method instance replaced-args))
    (catch Throwable e
      (messages/method-call-error slim-id method-name args e))))

(defn ^{:future-ns :statement-executor-impl}
call-and-assign [variable slim-id method-name args]
  (try
    (let [result (call-fixture-method slim-id method-name args)]
      (variables/assign-slim-variable variable result)
      result)
    (catch Exception e
      (messages/unexpected-error e))))

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

