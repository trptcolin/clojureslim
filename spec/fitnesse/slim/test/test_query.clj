(ns fitnesse.slim.test.test-query)

(defn new [n] (atom {:n (Integer/parseInt n)}))

(defn query [state]
  (vec (for [n (range 1 (inc (:n @state)))]
         [["n" n]
          ["2n" (* 2 n)]])))

(defn table [& _])
