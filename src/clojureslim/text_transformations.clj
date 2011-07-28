(ns clojureslim.text-transformations
  (:require [clojure.string :as string]))

(defn underscorize [s]
  (.replaceAll s "-" "_"))

(defn dasherize [s]
  (string/join "-"
    (map (comp #(.replaceAll % "-" "") string/lower-case)
         (drop-while string/blank? (.split s "(?=[^a-z])")))))

(defn pr-str-stack-trace [e]
  (string/join "\n" (cons e (.getStackTrace e))))
