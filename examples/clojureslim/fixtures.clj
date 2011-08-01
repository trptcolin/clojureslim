(ns clojureslim.fixtures
  (:require [clojureslim.statement-executor :as se])
  (:import [java.io Writer]
           [fitnesse.fixtures SplitFixture
                              DuplicateRows]))

(defrecord TestSlim [constructor-arg state]
  Object
  (toString [this]
    (if constructor-arg
      (str "TestSlim: " constructor-arg ", " (:string @state))
      (str "TestSlim: null, " @state))))

(defn make-fixture
  ([] (make-fixture nil))
  ([constructor-arg]
   (TestSlim. constructor-arg (atom {})))
  ([constructor-arg base-fixture]
   (TestSlim. constructor-arg (atom @(:state base-fixture)))))

(defn echo [this thing-to-echo]
  thing-to-echo)

(defn echo-fixture []
  (make-fixture))

(defn echo-string [this s]
  s)

(defn echo-int [this i]
  i)

(defn test-slim
  ([]
   (make-fixture 0))
  ([& args]
    (apply make-fixture args)))

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
  (swap! (:state this) assoc :string in))

(defn get-string-arg [this]
  (:string @(:state this)))

(defn is-same [this other]
  (= this other))

(defn get-string-from-other [this other]
  (get-string-arg other))

(defn return-constructor-arg [this]
  (:constructor-arg this))

(defn output [this]
  (let [in (:input @this)]
    (* in 2)))

(defprotocol QueryTable
  (query [this]))

; whole table
; first [and only] row
; first column  (label-value)
; second column (label-value)
(defrecord TestQuery [args]
  QueryTable
  (query [this]
    [[["n" 1]
      ["2n" 2]]]))

(defn test-query [& args]
  (TestQuery. args))


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


; TODO: extract?
(extend-type SplitFixture
  QueryTable
  (query [this]
    (.query this)))

(defn split-fixture [string-to-split]
  (SplitFixture. string-to-split))

(defn null-fixture
  ([] (make-fixture))
  ([s] (make-fixture)))

(defn get-null [this]
  nil)

(defn get-blank [this]
  "")

(defrecord MySystemUnderTestDriver [system-under-test called])

(defn make-system-under-test []
  {:called (atom false)})

(defn foo [this]
  (swap! (:called this) not))
(def bar
  (comp foo :system-under-test))

(defn driver-called [this]
  @(:called this))
(defn system-under-test-called [this]
  @(:called (:system-under-test this)))


(defn my-system-under-test-driver []
  (MySystemUnderTestDriver. (make-system-under-test)
                            (atom false)))

(defn dummy-table-table-returns-null []
  (make-fixture))
(defn do-table [this args]
  )

(extend-type DuplicateRows
  QueryTable
  (query [this]
    (.query this)))

(defn duplicate-rows [arg]
  (DuplicateRows. arg))

; TODO: avoid the need to do things like this by ordering the search path
(defn set-the-name [this s]
  (swap! (:state this) assoc :the-name s))
(defn the-name [this]
  (:the-name @(:state this)))


(defn concatenate-three-args [this a b c]
  (str a " " b " " c))

(defn login-dialog-driver [username password]
  (make-fixture [username password]))

(defn login-with-username-and-password [this username password]
  (= (:constructor-arg this) [username password]))
