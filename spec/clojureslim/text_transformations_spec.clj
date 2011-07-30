(ns clojureslim.text-transformations-spec
  (:use [speclj.core]
        [clojureslim.text-transformations]))

(describe "dasherize"

  (it "leaves simple cases alone"
    (should= "foo" (dasherize "foo")))

  (it "downcases a single word"
    (should= "foo" (dasherize "Foo")))

  (it "splits camel cases with a dash"
    (should= "foo-bar" (dasherize "FooBar")))

  (it "leaves already-dashed cases alone"
    (should= "foo-bar" (dasherize "foo-bar")))

  (it "converts snake case"
    (should= "foo-bar" (dasherize "foo_bar"))))



