(ns clojureslim.executor-spec
  (:require [speclj.core :refer :all]
            [clojureslim.executor :refer :all]
            [clj-stacktrace.repl :as st])
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

  (before
    (doseq [ns (filter #(.startsWith (name %) "clojureslim.fixtures") (map #(.getName %) (all-ns)))]
      (unload-ns ns)))
  (around [it]
    (with-redefs [st/pst identity]
      (it)))

  (it "starting state"
    (should= false (.stopHasBeenRequested @executor))
    (should= [] (:paths @(.state @executor)))
    (should= {} (:instances @(.state @executor)))
    (should= [] (:libraries @(.state @executor)))
    )

  (it "adds paths"
    (.addPath @executor "foo.bar")
    (should= ["foo.bar"] (:paths @(.state @executor)))
    (.addPath @executor "foo.bar")
    (should= ["foo.bar"] (:paths @(.state @executor)))
    (.addPath @executor "fizz.bang")
    (should= ["foo.bar" "fizz.bang"] (:paths @(.state @executor))))

  (it "create an instances"
    (.addPath @executor "clojureslim.fixtures")
    (.create @executor "name_1" "Echo" (into-array Object []))
    (let [instance (get (:instances @(.state @executor)) "name_1")]
      (should-not-be-nil instance)
      (should= "clojure.lang.Atom" (.getName (.getClass instance)))
      (should= 'clojureslim.fixtures.echo (:ns (meta instance)))
      (should= "nothing" (:message @instance))))

  (it "create missing fixture"
    (.addPath @executor "clojureslim.nowhere")
    (try
      (.create @executor "name_1" "Echo" (into-array Object []))
      (should-fail "No SlimException")
      (catch SlimException e
        (should= SlimServer/NO_CONSTRUCTOR (.getTag e)))))

  (it "create with bad arity"
    (.addPath @executor "clojureslim.fixtures")
    (try
      (.create @executor "name_1" "Echo" (into-array Object [1 2 3]))
      (should-fail "No SlimException")
      (catch SlimException e
        (should= SlimServer/COULD_NOT_INVOKE_CONSTRUCTOR (.getTag e)))))

  (it "create returning non-atom"
    (.addPath @executor "clojureslim.fixtures")
    (try
      (.create @executor "name_1" "NonAtomCtor" (into-array Object []))
      (should-fail "No SlimException")
      (catch SlimException e
        (should= "Fixture constructors must return an atom/ref" (.getMessage e)))))

  (it "calls a method"
    (.addPath @executor "clojureslim.fixtures")
    (.create @executor "name_1" "Echo" (into-array Object []))
    (let [instance (get (:instances @(.state @executor)) "name_1")]
      (should= "Hello!" (.call @executor "name_1" "echo" (into-array Object ["Hello!"])))
      (should= "Hello!" (:message @instance))))

  (it "adds a library"
    (.addPath @executor "clojureslim.fixtures")
    (.create @executor "library_1" "Echo" (into-array Object []))
    (let [instance (first (:libraries @(.state @executor)))]
      (should-be-same instance (get (:instances @(.state @executor)) "library_1"))
      (should-not-be-nil instance)
      (should= "clojure.lang.Atom" (.getName (.getClass instance)))
      (should= 'clojureslim.fixtures.echo (:ns (meta instance)))
      (should= "nothing" (:message @instance))))

  (it "attemps calls on libraries"
    (.addPath @executor "clojureslim.fixtures")
    (.create @executor "library_1" "Echo" (into-array Object []))
    (should= "Hello!" (.call @executor "name_1" "echo" (into-array Object ["Hello!"]))))

  )
