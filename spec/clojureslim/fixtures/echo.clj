(ns clojureslim.fixtures.echo)

(defn new [] (atom {:message "nothing"}))

(defn echo [state message]
  (swap! state assoc :message message)
  message)