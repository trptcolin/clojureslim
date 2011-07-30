(ns clojureslim.variables-spec
  (:use [clojureslim.variables]
        [speclj.core]))

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
               "$FOO-query-$THEKEY")))

  (it "allows similarly-named variables"
    (should= "foo-bar-baz"
             (replace-slim-variables-in-string
               {"VALUE" "bar"
                "VALUEX" "baz"}
               "foo-$VALUE-$VALUEX")))

  (it "replaces with non-string variable values"
    (let [container (atom {})]
      (should= container
               (replace-slim-variables-in-string
                 {"FOO" container}
                 "$FOO")))))
