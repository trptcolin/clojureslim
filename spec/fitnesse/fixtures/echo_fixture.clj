(ns fitnesse.fixtures.echo-fixture)

(defn name [state] (:name @state))
(defn set-name [state name] (swap! state assoc :name name))
(defn name-contains [state s] (.contains (:name @state) s))
(defn echo [_ message] message)
(defn echo-int [_ i] i)
(defn echo-and-log [_ s] (println s) s)
