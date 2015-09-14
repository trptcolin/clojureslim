(ns clojureslim.variables-spec
  (:use [clojureslim.variables]
        [speclj.core]))

(describe "Variables"

  (it "gives the argument back by default"
    (should= "foo" (replace-slim-variables-in {} "foo")))

  (it "replaces in the simplest case"
    (should= "something"
             (replace-slim-variables-in
               {"THEKEY" "something"}
               "$THEKEY")))

  (it "replaces when the variable is in the middle"
    (should= "query-something"
             (replace-slim-variables-in
               {"THEKEY" "something"}
               "query-$THEKEY")))

  (it "replaces multiple variables"
    (should= "bar-query-something"
             (replace-slim-variables-in
               {"THEKEY" "something"
                "FOO" "bar"}
               "$FOO-query-$THEKEY")))

  (it "allows similarly-named variables"
    (should= "foo-bar-baz"
             (replace-slim-variables-in
               {"VALUE" "bar"
                "VALUEX" "baz"}
               "foo-$VALUE-$VALUEX")))

  (it "replaces with non-string variable values"
    (let [container (atom {})]
      (should= container
               (replace-slim-variables-in
                 {"FOO" container}
                 "$FOO")))))
