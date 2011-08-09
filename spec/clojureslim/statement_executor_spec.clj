(ns clojureslim.statement-executor-spec
  (:use [speclj.core]
        [clojureslim.statement-executor]))

(describe "statement-executor"
  (with executor (make-statement-executor))

  (it "adds paths successfully"
    (binding [require (fn [x] (pr x))]
      (should= "required-library"
               (with-out-str
                 (should= "OK" (.addPath @executor "required-library"))))))

  (it "bombs with a fitnesse-friendly message when require fails"
    (binding [require (fn [x] (throw (java.io.FileNotFoundException. "whoops")))]
      (should= "__EXCEPTION__: java.io.FileNotFoundException: whoops"
               (.addPath @executor "missing-library"))))


)
