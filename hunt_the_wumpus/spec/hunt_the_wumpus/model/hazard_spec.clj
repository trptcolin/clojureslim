(ns hunt-the-wumpus.model.hazard-spec
  (:use
    [speclj.core]
    [hunt-the-wumpus.model.hazard]
    [hunt-the-wumpus.model.game :only (new-game)]
    [hunt-the-wumpus.model.player :only (place-player)]
    [hunt-the-wumpus.model.map :only (donut-map)]))

(describe "Hazards"

  (it "can place a hazard"
    (let [game (place-hazard (new-game) :wumpus 1)]
      (should= [1] (:wumpus (:hazards game)))))

  (it "can place a hazard as keyword"
    (let [game (place-hazard (new-game) "wumpus" 1)]
      (should= [1] (:wumpus (:hazards game)))))

  (it "knows that wumpus is a hazard"
    (should= true (hazard? "wumpus"))
    (should= true (hazard? :wumpus)))

  (it "knows that bats is a hazard"
    (should= true (hazard? "bats"))
    (should= true (hazard? :bats)))
  )
