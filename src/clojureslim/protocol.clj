(ns clojureslim.protocol
  (:require [clj-stacktrace.repl :as st]))

(def unreported-exception-function-names
  #{"table" "begin-table" "end-table" "reset" "execute"})

(def exception-tag "__EXCEPTION__:")

(defn success []
  "OK")

(defn format-error [& args]
  (when-not (System/getProperty "clojureslim.nodebug")
    (apply print args))
  (apply print-str args))

(defn no-constructor-error [name args]
  (format-error exception-tag
                (format "message:<<NO_CONSTRUCTOR %s>>"
                        name)))

(defn constructor-error [name args e]
  (format-error
    exception-tag
    (format "message:<<COULD_NOT_INVOKE_CONSTRUCTOR %s[%d]>>"
            name
            (count args))
    (st/pst-str e)))

(defn unexpected-error [e]
  (format-error exception-tag e "\n" (st/pst-str e)))

(defn library-error [library-name]
  (format-error exception-tag " no library found for " library-name))

(defn method-missing-error [slim-id method-name args e]
  (if (unreported-exception-function-names method-name)
    nil
    (format-error exception-tag
                  (format "message:<<NO_METHOD_IN_CLASS %s[%d] %s>>"
                          method-name
                          (count args)
                          slim-id)
                  (st/pst-str e))))

(defn method-invokation-error [e]
  (format-error exception-tag (str "message:" (st/pst-str e))))

