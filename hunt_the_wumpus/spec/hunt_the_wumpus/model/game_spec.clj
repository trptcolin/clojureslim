(ns hunt-the-wumpus.model.game-spec
  (:use
    [speclj.core]
    [hunt-the-wumpus.model.game]
    [hunt-the-wumpus.model.report :only (hazard-report player-report item-report game-over-report)]
    [hunt-the-wumpus.model.item :only (items-of items-in place-item)]
    [hunt-the-wumpus.model.player :only (place-player kill-player player-dead?)]
    [hunt-the-wumpus.model.hazard :only (place-hazard)]))

(describe "Game"

  (it "creating an empty game"
    (let [game (new-game)]
      (should= {} (:caverns game))
      (should= {} (:hazards game))
      (should= {} (:items game))
      (should= {} (:players game))))

  (it "creates a populated game"
    (let [game (new-game :caverns {1 {}} :hazards {:pits [1]} :items {:arrows [1]} :players {"joe" {}})]
      (should= {1 {}} (:caverns game))
      (should= {:pits [1]} (:hazards game))
      (should= {:arrows [1]} (:items game))
      (should= {"joe" {}} (:players game))))

  (it "performs a command"
    (let [game-ref (ref (new-game :caverns {1 {:east 2}} :players {"player-1" {:cavern 1}}))
          report (perform-command game-ref "player-1" (fn [game player] (assoc game :foo player)))]
      (should= "player-1" (:foo @game-ref))
      (should= ["You can go east."] (:cavern-messages report))))

  (context "with arrows"
    (with game
      (ref
        (-> (new-game :caverns {1 {:east 2}})
          (place-player "Thor" 1)
          (place-item :arrow 1))))

    (it "players pick up items in the current room"
      (should= [:arrow] (items-in @@game 1))
      (perform-command @game "Thor" (fn [game player] game))
      (should= [] (items-in @@game 1))
      (should= [:arrow] (items-of @@game "Thor")))

    (it "player dies when meeting the wumpus"
      (dosync
        (alter @game place-hazard "wumpus" 1))
      (let [command (fn [game player] game)]
        (perform-command @game "Thor" command)
        (should= true (player-dead? @@game "Thor"))))

    (it "dead players are not allowed to do stuff"
      (dosync
        (alter @game kill-player "Thor" "wumpus"))
      (let [command (fn [game player] (assoc game :fooey true))]
        (perform-command @game "Thor" command)
        (should= nil (:fooey @@game))))

    (it "reports a player is dead"
      (dosync
        (alter @game kill-player "Thor" "wumpus"))
      (let [report (perform-command @game "Thor" (fn [game player] game))]
        (should= "Your game is over. Go in peace." (:error report))))
    )


  (it "reports hazards"
    (with-redefs [hazard-report (fn [& args] ["Woohoo!"])]
      (let [report (perform-command (ref (new-game)) "player-1" (fn [game player]))]
        (should= ["Woohoo!"] (:hazard-messages report)))))

  (it "reports the end of the game"
    (with-redefs [game-over-report (fn [& args] ["Oh noez..."])]
      (let [report (perform-command (ref (new-game)) "player-1" (fn [game player]))]
        (should= {:game-over-messages ["Oh noez..."]}
          report))))

  (it "reports player events"
    (with-redefs [player-report (fn [& args] ["Yahoo!"])]
      (let [report (perform-command (ref (new-game)) "player-1" (fn [game player]))]
        (should= ["Yahoo!"] (:player-messages report)))))

  (it "reports on items"
    (with-redefs [item-report (fn [& args] ["Stuff!"])]
      (let [report (perform-command (ref (new-game)) "player-1" (fn [game player]))]
        (should= ["Stuff!"] (:item-messages report)))))
  )

