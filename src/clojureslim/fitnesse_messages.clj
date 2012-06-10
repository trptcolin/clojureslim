(ns clojureslim.fitnesse-messages
  (:use [clojureslim.text-transformations :as tt]))

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
            (count args)
            e
            (tt/pr-str-stack-trace e))))

(defn unexpected-error [e]
  (format-error exception-tag e "\n" (tt/pr-str-stack-trace e)))

(defn library-error [library-name]
  (format-error exception-tag " no library found for " library-name))

(defn method-call-error [slim-id method-name args e]
  (if (unreported-exception-function-names method-name)
      nil
      (format-error exception-tag
                    (format "message:<<NO_METHOD_IN_CLASS %s[%d] %s>>"
                            method-name
                            (count args)
                            slim-id)
                    (tt/pr-str-stack-trace e))))

