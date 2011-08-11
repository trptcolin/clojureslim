(ns clojureslim.variables
  (:require [clojure.string :as string]))

(def ^{:private true}
  slim-variables (atom {}))

(defn get-slim-variable-value [slim-variables word]
  (get slim-variables
       (string/replace word #"\$" "")
       word))

(defn replace-slim-variables-in
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
      ; TODO: this seems a little funny - just arbitrarily using the first replacement, which could be a string
      (first replacements))))

; TODO: test this
(defn ^{:public-api true}
  assign-slim-variable
  [variable value]
  (swap! slim-variables assoc variable value))

(defmulti ^{:public-api true}
  replace-slim-variables
  "Finds Slim variables in the given objects from the given map of variables,
  designated by $ followed by any word characters (\\w in a regex sense, just
  like [a-zA-Z_]), and attempts to replace them from the map. Falls back to
  leaving the variable name in place."
  type)

(defmethod replace-slim-variables
  java.lang.String
  [arg]
  (replace-slim-variables-in @slim-variables arg))

(defmethod replace-slim-variables
  clojure.lang.Seqable
  [args]
  (map (partial replace-slim-variables-in @slim-variables) args))

(defmethod replace-slim-variables
  (Class/forName "[Ljava.lang.Object;")
  [args]
  (map (partial replace-slim-variables-in @slim-variables) args))

(defmethod replace-slim-variables :default [args]
  args)
