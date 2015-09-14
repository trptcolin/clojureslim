(ns hunt-the-wumpus.fixtures.make-map
  (:require [hunt-the-wumpus.fixtures.context :as context]
            [hunt-the-wumpus.model.commands :refer [translate-direction translate-command]]
            [hunt-the-wumpus.model.map :refer [add-paths opposite-direction]]))

(defn new [] (atom {:map       {}
                    :start     nil
                    :end       nil
                    :direction nil}))

(defn set-start [state start] (swap! state assoc :start start))
(defn set-end [state end] (swap! state assoc :end end))
(defn set-direction [state direction] (swap! state assoc :direction direction))

(defn execute [state]
  (swap! state update-in [:map] add-paths
         :start (:start @state)
         :end (:end @state)
         :direction (translate-direction (:direction @state))))

(defn end-table [state]
  (dosync
    (alter context/game-ref assoc :caverns (:map @state))))