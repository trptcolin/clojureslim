(ns clojureslim.variables
  (:require [clojure.string :as string]))

(defn- get-slim-variable-value [slim-variables word]
  (get slim-variables
                 (string/replace word #"\$" "")
                 word))

; TODO: do we ever have a weird case of multiple non-string values?
(defn replace-slim-variables-in-string
  "Finds Slim variables in the given string from the given map of variables,
  designated by $ followed by any word characters (\\w in a regex sense, just
  like [a-zA-Z_]), and attempts to replace them from the map. Falls back to
  leaving the variable name in place."
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


(defn replace-slim-variables [slim-variables args]
  (map (partial replace-slim-variables-in-string slim-variables) args))
