(ns clojureslim.clj-test)


(defn fixture-set [fixture field value]
  (swap! (:state fixture) assoc field value))

(defn something-happened [this]
  (:something-happened? @(:state this)))

(defn run [this]
  (fixture-set this :something-happened? (not (something-happened this))))

(defrecord CljTest [state])

(defn clj-test-first [& args]
  (CljTest. (atom {:something-happened? false})))


(defn echo [this thing-to-echo]
  thing-to-echo)

(defn echo-fixture []
  (CljTest. nil))

(defn test-slim []
  (println "constructing test-slim")
  (CljTest. (atom {})))

(defn echo-boolean [this arg]
  (println "echo-boolean args: " arg)
  (boolean arg))


(defn should-i-buy-milk []
  (CljTest. (atom {})))

(defn set-cash-in-wallet [this x]
  (fixture-set this :cash-in-wallet (Integer/parseInt x)))

(defn set-credit-card [this x]
  (fixture-set this :credit-card (= "yes" x)))

(defn set-pints-of-milk-remaining [this x]
  (fixture-set this :pints-of-milk-remaining (Integer/parseInt x)))

(defn go-to-store [this]
  (let [state @(:state this)]
    (if (and (= 0 (:pints-of-milk-remaining state))
             (or (< 2 (:cash-in-wallet state))
                 (:credit-card state)))
      "yes"
      "no")))




(defn some-decision-table []
  (CljTest. (atom {})))

;(defmulti set-input [this in] #(type %2))
(defn set-input [this in]
  (fixture-set this
               :input
               (Integer/parseInt in)))

(defn output [this]
  (let [in (:input @(:state this))]
    (println "fetching input in the output getter: " in)
    (* in 2)))
