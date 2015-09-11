(ns clojureslim.fixtures.echo)

(defprotocol Ech
  (echo [_ message]))
(deftype Echo [message]
  Ech
  (echo [_ m] (reset! message m))
  )
(defn new-echo [] (Echo. (atom "nothing")))
