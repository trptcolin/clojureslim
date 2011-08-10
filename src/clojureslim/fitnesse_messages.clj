(ns clojureslim.fitnesse-messages
  (:use [clojureslim.text-transformations :as tt]))

(def unreported-exception-function-names
  #{"table" "begin-table" "end-table" "reset" "execute"})

(def exception-tag "__EXCEPTION__:")

(defn success []
  "OK")

(defn constructor-error [name args]
  (print-str
    exception-tag
    (format "message:<<COULD_NOT_INVOKE_CONSTRUCTOR %s[%d]>>"
            name
            (count args))))

(defn unexpected-constructor-error [fixture-name e]
  (print-str exception-tag
             "Problem creating fixture for" fixture-name
             "\n" (tt/pr-str-stack-trace e)))

(defn library-error [e]
  (str exception-tag " " e))

(defn method-call-error [slim-id method-name args e]
  (if (unreported-exception-function-names method-name)
      nil
      (print-str exception-tag
                 (format "message:<<NO_METHOD_IN_CLASS %s[%d] %s>>"
                         method-name
                         (count args)
                         slim-id)
                 (tt/pr-str-stack-trace e))))

