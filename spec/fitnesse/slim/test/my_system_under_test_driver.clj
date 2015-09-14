(ns fitnesse.slim.test.my-system-under-test-driver
  (:require [clojureslim.java :as j]))

(j/wrap-java-fixture fitnesse.slim.test.MySystemUnderTestDriver)
