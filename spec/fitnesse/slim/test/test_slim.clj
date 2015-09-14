(ns fitnesse.slim.test.test-slim
  (:require [clojureslim.java :as java])
  (:import (fitnesse.slim.test TestSlim)))

(java/wrap-java-fixture fitnesse.slim.test.TestSlim)

(defn new
  ([] (atom (TestSlim.)))
  ([arg] (atom (TestSlim. (Integer/parseInt arg))))
  ([arg other] (atom (TestSlim. (Integer/parseInt arg) @other))))

(defn create-test-slim-with-string [this arg]
  (atom (.createTestSlimWithString @this arg) :meta {:ns 'fitnesse.slim.test.test-slim}))

(defn get-string-from-other [this other]
  (.getStringFromOther @this @other))

(defn is-same [this other]
  (= @this @other))