(ns fitnesse.fixtures.set-up
  (:require [clojureslim.java :as j]))

(j/wrap-java-fixture fitnesse.fixtures.SetUp)

(defn table [_ _] #_ignore)