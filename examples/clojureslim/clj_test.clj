(ns clojureslim.clj-test)

(defn clj-test-first [] {})

(def happening (atom false))
(defn something-happened [fixture] @happening)
(defn run [fixture] (swap! happening (constantly true)))

