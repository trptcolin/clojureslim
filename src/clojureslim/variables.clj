(ns clojureslim.variables
  (:require [clojure.string :as str])
  (:import (clojure.lang Seqable)))

(defn get-slim-variable-value [slim-variables word]
  (get slim-variables
       (str/replace word #"\$" "")
       word))

(defn replace-slim-variables-in [vars val]
  (assert (or (string? val) "val is not a String!"))
  (let [variable-attempts (reverse (sort-by count (re-seq #"\$[^$\W]*" val)))
        replacements (map (partial get-slim-variable-value vars) variable-attempts)]
    (if (every? string? replacements)
      (reduce
        #(str/replace %1 %2 (get-slim-variable-value vars %2))
        val
        variable-attempts)
      ; TODO: this seems a little funny - just arbitrarily using the first replacement, which could be a string
      (first replacements))))

(defmulti replace-slim-variables
  "Finds Slim variables in the given objects from the given map of variables,
   designated by $ followed by any word characters (\\w in a regex sense, just
   like [a-zA-Z_]), and attempts to replace them from the map. Falls back to
   leaving the variable name in place."
  (fn [vars arg] (type arg)))

(defmethod replace-slim-variables
  CharSequence
  [vars arg]
  (replace-slim-variables-in vars arg))

(defmethod replace-slim-variables
  Seqable
  [vars args]
  (mapv (partial replace-slim-variables vars) args))

(defmethod replace-slim-variables
  (Class/forName "[Ljava.lang.Object;")
  [vars args]
  (mapv (partial replace-slim-variables vars) args))

(defmethod replace-slim-variables :default [vars args]
  args)
