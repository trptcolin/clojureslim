(ns hunt-the-wumpus.model.player-spec
  (:use
    [speclj.core]
    [hunt-the-wumpus.model.player]
    [hunt-the-wumpus.model.game :only (new-game)]
    [hunt-the-wumpus.model.game :only (new-game)]))

(describe "player"

  (it "joins the game"
    (let [game (new-game :caverns {1 {}})
          after (join-game game "Thor")]
      (should= {"Thor" {:cavern 1}} (:players after))))

  (it "moves a player to the same location"
    (let [game {:players {"Thor" {:cavern 1}}}]
      (move-player-to-location game "Thor" 1)
      (should= 1 (player-location game "Thor"))))

  (it "moves a player to a different location"
    (let [game {:players {"Thor" {:cavern 1}}}
          game (move-player-to-location game "Thor" 5)]
      (should= 5 (player-location game "Thor"))))

  (it "moves a player in a direction"
    (let [game {:players {"Thor" {:cavern 1}}
                :caverns {1 {:east 2}}}
          game (move-player-in-direction game "Thor" :east)]
      (should= 2 (player-location game "Thor"))))

  (it "moves a player in a wrong direction"
    (let [game {:players {"Thor" {:cavern 1}}
                :caverns {1 {:west 2}}}]
      (should-throw
        Exception "You can't go east from here."
        (move-player-in-direction game "Thor" :east))))

  (it "moves a player in an illegal direction"
    (let [game {:players {"Thor" {:cavern 1}}
                :caverns {1 {:west 2}}}]
      (should-throw (move-player-in-direction game "Thor" "E"))))

  (it "kills a player"
    (let [game {:players {"Thor" {:cavern 1}}}
          game (kill-player game "Thor" "wumpus")]
      (should= true (player-dead? game "Thor"))))

  (it "Shouldn't kill Thor"
    (let [game {:players {"Thor" {:cavern 1}}}
          thor (some
                (fn [[player-name player-attrs]]
                  (if (= "Thor" player-name)
                    player-attrs
                    nil))
                (:players game))]
      (should= {:cavern 1} thor)
      (should-not (player-dead? game thor))))

  )

