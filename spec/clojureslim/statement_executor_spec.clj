(ns clojureslim.statement-executor-spec
  (:require [speclj.core :refer :all]
            [clojureslim.statement-executor :refer :all]))

(describe "statement-executor"
  (with executor (make-statement-executor))

  (it "adds paths successfully"
    (with-redefs [require (fn [x] (pr x))]
      (should= "required-library"
               (with-out-str (.addPath @executor "required-library")))))

  (it "bombs with a fitnesse.sh-friendly message when require fails"
    (with-redefs [require (fn [x] (throw (java.io.FileNotFoundException. "whoops")))]
      (let [result (with-out-str (.addPath @executor "missing-library"))]
        (should-contain "java.io.FileNotFoundException" result)
        (should-contain "whoops" result))))

  )
