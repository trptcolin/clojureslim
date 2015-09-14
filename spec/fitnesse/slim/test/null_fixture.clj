(ns fitnesse.slim.test.null-fixture)


(defn new
  ([] (atom nil))
  ([x] (atom nil)))

(defn get-null [_] nil)
(defn get-blank [_] "")