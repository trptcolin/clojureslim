(ns hunt-the-wumpus.fixtures.context
  (:require [hunt-the-wumpus.model.game :as game]))


(def game-ref (ref (game/new-game)))
(def last-report (atom nil))