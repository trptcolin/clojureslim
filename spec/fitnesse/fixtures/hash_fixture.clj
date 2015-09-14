(ns fitnesse.fixtures.hash-fixture)

(defn send-as-hash [state hash] (reset! state hash))
(defn hash [state] (into {} @state))
(defn hash-is [state key] (get @state key))
