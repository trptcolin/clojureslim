(ns clojureslim.executor-spec
  (:require [speclj.core :refer :all]
            [clojureslim.executor :refer :all])
  (:import (fitnesse.slim SlimException SlimServer)))

(deftype Echo [state])
(defn new-echo [] (Echo. (atom "nothing")))

(describe "executor"
  (with executor (new-executor))

  ;(it "adds paths successfully"
  ;  (with-redefs [require (fn [x] (pr x))]
  ;    (should= "required-library"
  ;             (with-out-str (.addPath @executor "required-library")))))
  ;
  ;(it "bombs with a fitnesse.sh-friendly message when require fails"
  ;  (with-redefs [require (fn [x] (throw (java.io.FileNotFoundException. "whoops")))]
  ;    (let [result (with-out-str (.addPath @executor "missing-library"))]
  ;      (should-contain "java.io.FileNotFoundException" result)
  ;      (should-contain "whoops" result))))

  ;(it "calls method on give instance before trying to invoke on system under test"
  ;  (let [echo ()])
  ;  MyAnnotatedSystemUnderTestFixture myInstance = createAnnotatedFixture();
  ;    Object result = statementExecutor.call(INSTANCE_NAME, echoMethodName());
  ;    assertEquals(voidMessage(), result);
  ;    assertTrue(myInstance.echoCalled());
  ;    assertFalse(myInstance.getSystemUnderTest().speakCalled());
  ;  )

  (it "starting state"
    (should= false (.stopHasBeenRequested @executor))
    (should= [] (:nses @(.state @executor)))
    )

  (it "adds paths"
    (.addPath @executor "clojureslim.fixtures.echo")
    (should= ['clojureslim.fixtures.echo] (:nses @(.state @executor)))
    (.addPath @executor "clojureslim.fixtures.echo")
    (should= ['clojureslim.fixtures.echo] (:nses @(.state @executor)))
    (.addPath @executor "clojureslim.fixtures.null")
    (should= ['clojureslim.fixtures.echo 'clojureslim.fixtures.null] (:nses @(.state @executor))))

  (it "create an instances"
    (.addPath @executor "clojureslim.fixtures.echo")
    (.create @executor "name_1" "Echo" (into-array Object []))
    (let [instance (get (:instances @(.state @executor)) "name_1")]
      (should-not-be-nil instance)
      (should= "clojureslim.fixtures.echo.Echo" (.getName (.getClass instance)))))

  (it "create missing fixture"
    (try
      (.create @executor "name_1" "Echo" (into-array Object []))
      (should-fail "No SlimException")
    (catch SlimException e
      (should= SlimServer/NO_CONSTRUCTOR (.getTag e)))))

  (it "create with bad arity"
    (.addPath @executor "clojureslim.fixtures.echo")
    (try
      (.create @executor "name_1" "Echo" (into-array Object [1 2 3]))
      (should-fail "No SlimException")
    (catch SlimException e
      (should= SlimServer/COULD_NOT_INVOKE_CONSTRUCTOR (.getTag e)))))

  (it "calls a method"
    (.addPath @executor "clojureslim.fixtures.echo")
    (.create @executor "name_1" "Echo" (into-array Object []))
    (let [instance (get (:instances @(.state @executor)) "name_1")]
      (should= "Hello!" (.call @executor "name_1" "echo" "Hello!"))
      (should= "Hello!" @(.message instance)))
    )

  )
