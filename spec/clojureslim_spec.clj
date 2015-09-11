(ns clojureslim-spec
  (:require [speclj.core :refer :all]
            [clojureslim :as clim])
  (:import (fitnesse.slim NameTranslator StatementExecutorInterface)))

(describe "Clojure Slim"

  (it "translates names"
    (should= "foo" (.translate clim/translator "foo"))
    (should= "foo" (.translate clim/translator "Foo"))
    (should= "foo-bar" (.translate clim/translator "FooBar"))
    (should= "foo-bar" (.translate clim/translator "fooBar"))
    (should= "foo-bar" (.translate clim/translator "foo_bar")))

  (it "slim factory translator"
    (let [factory (clim/clojure-slim-factory)]
      (should-be-same clim/translator (.getMethodNameTranslator factory))
      (should-be-a StatementExecutorInterface (.getStatementExecutor factory))))

  )
