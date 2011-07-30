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

