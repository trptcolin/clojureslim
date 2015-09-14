(ns clojureslim.fixtures.echo)

(defn new
  ([] (atom {:message "nothing"}))
  ([message] (atom {:message message})))

(defn echo [state message]
  (swap! state assoc :message message)
  message)