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

(describe "replace-slim-variables-in-string"

  (it "gives the argument back by default"
    (should= "foo" (replace-slim-variables-in-string {} "foo")))

  (it "replaces in the simplest case"
    (should= "something"
             (replace-slim-variables-in-string
               {"THEKEY" "something"}
               "$THEKEY")))

  (it "replaces when the variable is in the middle"
    (should= "query-something"
             (replace-slim-variables-in-string
               {"THEKEY" "something"}
               "query-$THEKEY")))

  (it "replaces multiple variables"
    (should= "bar-query-something"
             (replace-slim-variables-in-string
               {"THEKEY" "something"
                "FOO" "bar"}
               "$FOO-query-$THEKEY"))))

