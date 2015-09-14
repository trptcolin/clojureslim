(ns clojureslim.java
  (:require [chee.string :as cs])
  (:import (fitnesse.slim ConverterSupport SlimException)
           (java.lang.reflect Constructor Method)))

(defn ctor [klass args]
  (let [args (if args (into-array Object args) (into-array Object []))]
    (if-let [c ^Constructor (first (filter #(= (count args) (count (.getParameterTypes %))) (.getConstructors klass)))]
      (.newInstance c (ConverterSupport/convertArgs args (.getParameterTypes c)))
      (throw (SlimException. (str "Couldn't find appropriate constructor for class " (.getName klass)))))))

(defn invoke [klass instance method-name args]
  (let [args (into-array Object args)]
    (let [method ^Method (first (filter #(and (= method-name (.getName %))
                                              (= (count args) (count (.getParameterTypes %)))) (.getMethods klass)))]
      (.invoke method instance (ConverterSupport/convertArgs args (.getGenericParameterTypes method))))))

(defn method-calling-fn-forms [class-sym]
  (let [klass (.loadClass (ClassLoader/getSystemClassLoader) (name class-sym))]
    (let [method-names (set (map #(.getName %) (.getMethods klass)))]
      (for [name method-names]
        `(defn ~(symbol (cs/spear-case name)) [i# & args#] (invoke ~klass @i# ~name args#))))))

(defmacro wrap-java-fixture [fixture-class]
  `(do
     (defn new [& args#] (atom (ctor ~fixture-class args#)))
     ~@(method-calling-fn-forms fixture-class)))