(ns clojureslim.variables
  (:require [clojure.string :as string]))

(defn- get-slim-variable-value [slim-variables word]
  (get slim-variables
                 (string/replace word #"\$" "")
                 word))

(defmulti replace-slim-variables-in
  "Finds Slim variables in the given object from the given map of variables,
  designated by $ followed by any word characters (\\w in a regex sense, just
  like [a-zA-Z_]), and attempts to replace them from the map. Falls back to
  leaving the variable name in place."
  (comp type second vector))

; TODO: do we ever have a weird case of multiple non-string values?
(defmethod replace-slim-variables-in
  String
  [slim-variables initial-string]
  (let [variable-attempts (reverse (sort-by count
                                            (re-seq #"\$[^$\W]*" initial-string)))
        replacements (map (partial get-slim-variable-value slim-variables)
                          variable-attempts)]
    (if (every? string? replacements)
      (reduce
        #(string/replace %1 %2 (get-slim-variable-value slim-variables %2))
        initial-string
        variable-attempts)
      (first replacements))))

(defmethod replace-slim-variables-in
  :default
  [slim-variables initial-object]
  initial-object)

(defn replace-slim-variables [slim-variables args]
  (map (partial replace-slim-variables-in slim-variables) args))
