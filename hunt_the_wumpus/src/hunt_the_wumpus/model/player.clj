(ns hunt-the-wumpus.model.player
  (:use
    [hunt-the-wumpus.model.map :only (possible-paths)]))

(defn place-player [game player cavern]
  (update-in game [:players player] assoc :cavern cavern))

(defn player-location [game player]
  (:cavern (get (:players game) player)))

(defn join-game [game player-name]
  (let [start-cavern (first (sort (keys (:caverns game))))]
    (place-player game player-name start-cavern)))

(defn move-player-to-location [game player location]
  (update-in game [:players player] assoc :cavern location))

(defn move-player-in-direction [game player direction]
  {:pre [(some #{direction} [:east :west :north :south])]}
  (let [location (player-location game player)]
    (if-let [new-location (-> (possible-paths game location) (get direction))]
      (move-player-to-location game player new-location)
      (throw (Exception. (str "You can't go " (name direction) " from here."))))))

(defn go-north [game player] (move-player-in-direction game player :north))
(defn go-south [game player] (move-player-in-direction game player :south))
(defn go-east [game player] (move-player-in-direction game player :east))
(defn go-west [game player] (move-player-in-direction game player :west))

(defn kill-player [game player cause-of-death]
  (update-in game [:players player] assoc :cause-of-death cause-of-death))

(defn player-dead? [game player]
  (boolean (get-in game [:players player :cause-of-death])))