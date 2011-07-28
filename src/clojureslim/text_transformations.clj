(ns clojureslim.text-transformations
  (:require [clojure.string :as string]))

(defn dasherize
  "Converts the given string from camel case to a more idiomatic-Clojure dashed
  style."
  [s]
  (string/join
    "-"
    (let [parts (drop-while string/blank?
                            (.split s "(?=[^a-z])"))
          lowercase-parts (map string/lower-case parts)]
      (map #(string/replace % #"[^a-zA-Z]+" "") lowercase-parts))))

(defn pr-str-stack-trace [e]
  (string/join "\n" (cons e (.getStackTrace e))))

(defn- get-slim-variable-value [slim-variables word]
  (get slim-variables
                 (string/replace word #"\$" "")
                 word))

(defn replace-slim-variables-in-string
  "Finds Slim variables in the given string from the given map of variables,
  designated by $ followed by any word characters (\\w in a regex sense, just
  like [a-zA-Z_]), and attempts to replace them from the map. Falls back to
  leaving the variable name in place."
  [slim-variables initial-string]
  (let [variable-attempts (re-seq #"\$[^$\W]*" initial-string)]
    (reduce
      #(string/replace %1 %2 (get-slim-variable-value slim-variables %2))
      initial-string
      variable-attempts)))

(defn replace-slim-variables [slim-variables args]
  (map (partial replace-slim-variables-in-string slim-variables) args))
