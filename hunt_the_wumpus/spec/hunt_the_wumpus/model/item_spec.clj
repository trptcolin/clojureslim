(ns hunt-the-wumpus.model.item-spec
  (:use
    [speclj.core]
    [hunt-the-wumpus.model.item]
    [hunt-the-wumpus.model.game :only (new-game)]
    [hunt-the-wumpus.model.player :only (place-player)]
    [hunt-the-wumpus.model.map :only (donut-map)]))

(describe "Items"

  (it "can be places"
    (let [game (place-item (new-game) :arrow 1)]
      (should= [1] (:arrow (:items game)))))

  (it "are placed a item as keyword"
    (let [game (place-item (new-game) "arrow" 1)]
      (should= [1] (:arrow (:items game)))))

  (it "know what items are"
    (should= true (item? "arrow"))
    (should= true (item? :arrow)))

  (it "gives an arrow to a player"
    (let [game {:players {"Thor" {:cavern 1}}
                :caverns {1 {:west 2}}}
          game (add-items game "Thor" [:arrow])]
      (should= [:arrow] (:items (get (:players game) "Thor")))))

  (it "lists a single item"
    (let [game {:items {:arrow [1]}}]
      (should= [:arrow] (items-in game 1))))

  (it "lists the items in a cavern"
    (let [game {:items {:arrow [1 2 3 1] :bauble [1 2]}}]
      (should= [:arrow :arrow :bauble] (items-in game 1))
      (should= [:arrow :bauble] (items-in game 2))
      (should= [:arrow] (items-in game 3))
      (should= [] (items-in game 9))))

  (it "removes an item"
    (let [game {:items {:arrow [1 2]}}]
      (should= {:items {:arrow [1]}}
               (unplace-items game 2))))
  )


