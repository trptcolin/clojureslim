(ns fitnesse.fixtures.page-driver
  (:require [clojureslim.java :as j]))

(j/wrap-java-fixture fitnesse.fixtures.PageDriver)

(doseq [v (ns-publics (the-ns 'fitnesse.fixtures.page-driver))]
  (prn "v: " v))