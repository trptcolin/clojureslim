(ns hunt-the-wumpus.model.report
  (:use
    [clojure.set :only (difference)]
    [hunt-the-wumpus.model.map :only (possible-paths)]
    [hunt-the-wumpus.model.player :only (player-location)]
    [hunt-the-wumpus.model.item :only (items-of)]))

(defn cavern-report [game player]
  (map #(format "You can go %s." (name %))
    (keys (possible-paths game (player-location game player)))))

(defn- new-players [before after]
  (let [players-before (set (keys (:players before)))
        players-after (set (keys (:players after)))]
    (difference players-after players-before)))

(defn- new-player-report [new-player player]
  (if (= new-player player)
    "You have joined the game"
    (format "%s has joined the game" new-player)))

(defn player-report [before after player]
  (let [report []]
    (concat report (map #(new-player-report % player) (new-players before after)))))

(defmulti report-hazard identity)
(defmethod report-hazard :wumpus [hazard]
  "You smell the Wumpus.")
(defmethod report-hazard :bats [hazard]
  "You hear chirping.")

(defn- hazards-adjacent-to [game origin]
  (let [adjacent-caverns (set (vals (get (:caverns game) origin)))
        adjacent? (fn [[hazard caverns]] (some adjacent-caverns caverns))
        adjacent-hazard-pairs (filter adjacent? (:hazards game))]
    (map first adjacent-hazard-pairs)))

(defn hazard-report [game player]
  (let [origin (player-location game player)
        nearby-hazards (hazards-adjacent-to game origin)]
    (map report-hazard nearby-hazards)))

(defmulti report-game-over identity)
(defmethod report-game-over :wumpus [hazard]
  "You were killed by the Wumpus.")

(defn- game-enders-at [game location]
  (let [occupying-space? (fn [[hazard caverns]] (some #{location} caverns))
        hazards-occupying-space (filter occupying-space? (:hazards game))]
    (map first hazards-occupying-space)))

(defn game-over-report [game player]
  (let [origin (player-location game player)
        game-enders (game-enders-at game origin)]
    (map report-game-over game-enders)))

(defn- report-found-items [[item count]]
  (format
    "You found %s %s%s."
    (if (= 1 count) "an" count)
    (name item)
    (if (= 1 count) "" "s")))

(defn- report-current-items [[item count]]
  (format
    "You have %s %s%s."
    (if (= 0 count) "no" count)
    (name item)
    (if (= 1 count) "" "s")))

(defn subtract-items [before after]
  (into {}
    (map
      (fn [item] [item (- (or (get after item) 0) (or (get before item) 0))])
      (set (concat (keys before) (keys after))))))

(defn item-report [before after player]
  (let [items-before (frequencies (items-of before player))
        items-after (frequencies (items-of after player))
        new-items (subtract-items items-before items-after)]
    (concat
      (map report-found-items new-items)
      (map report-current-items (merge {:arrow 0} items-after)))))
