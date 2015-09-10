(ns hunt-the-wumpus.model.hazard
  (:use
    [hunt-the-wumpus.model.player :only (player-location)]))

(def hazards #{:wumpus :bats})

(defn hazard? [thing]
  (not (nil? (some hazards [(keyword thing)]))))

(defn place-hazard [game hazard cavern]
  (update-in game [:hazards (keyword hazard)] conj cavern))

(defn hazard-locations [game hazard]
  (get-in game [:hazards hazard]))
