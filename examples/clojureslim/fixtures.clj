(ns clojureslim.fixtures
  (:require [clojureslim.statement-executor :as se])
  (:import [java.io Writer]))

(defrecord TestSlim [constructor-arg state]
  Object
  (toString [this]
    (if constructor-arg
      (str "TestSlim: " constructor-arg ", " (:string @state))
      (str "TestSlim: null, " @state))))

(defn make-fixture [& args]
  (let [arg (first args)
        t (TestSlim. arg (atom {}))]
    (println "making fixture: " t)
    t))


(defn echo [this thing-to-echo]
  thing-to-echo)

(defn echo-fixture []
  (make-fixture))

(defn test-slim [& args]
  (let [[first-arg & others] args]
    (if first-arg
      (apply make-fixture args)
      (make-fixture 0))))

(defn echo-boolean [this arg]
  (boolean arg))



(defn should-i-buy-milk []
  (make-fixture))

(defn set-cash-in-wallet [this x]
  (swap! this assoc :cash-in-wallet (Integer/parseInt x)))

(defn set-credit-card [this x]
  (swap! this assoc :credit-card (= "yes" x)))

(defn set-pints-of-milk-remaining [this x]
  (swap! this assoc :pints-of-milk-remaining (Integer/parseInt x)))

(defn go-to-store [this]
  (let [state @this]
    (if (and (= 0 (:pints-of-milk-remaining state))
             (or (< 2 (:cash-in-wallet state))
                 (:credit-card state)))
      "yes"
      "no")))



(defn some-decision-table []
  (atom {}))

;(defmulti set-input [this in] #(type %2))
(defn set-input [this in]
  (swap! this assoc :input (Integer/parseInt in)))

(defn set-string [this in]
  (println (:state this))
  (swap! (:state this) assoc :string in))

(defn get-string-arg [this]
  (:string @(:state this)))

(defn output [this]
  (let [in (:input @this)]
    (* in 2)))


(defn test-query [& args]
  (atom {:args args}))

(defn query [& args]
  [           ; whole table
   [          ; first [and only] row
    ["n" 1]   ; first column  (label-value)
    ["2n" 2]  ; second column (label-value)
]])


(defn create-test-slim-with-string [this s]
  (let [fixture (make-fixture)]
    (set-string fixture s)
    fixture))


; TODO: this may actually need to be in the library proper
(def actor-instance-name "scriptTableActor")
(def fixture-stack (atom []))

(defn get-fixture [this]
  (let [executor (se/make-statement-executor)]
    (.getInstance executor actor-instance-name)))

(defn push-fixture [this]
  (swap! fixture-stack conj (get-fixture this)))

(defn pop-fixture [this]
  (let [popped (last @fixture-stack)]
    (swap! fixture-stack pop)
    (se/set-instance actor-instance-name popped)))

(defn uncle []
  (make-fixture))
