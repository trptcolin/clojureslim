(ns hunt-the-wumpus.model.game
  (:use
    [hunt-the-wumpus.model.item :only (items-in add-items unplace-items place-item)]
    [hunt-the-wumpus.model.hazard :only (place-hazard hazard-locations)]
    [hunt-the-wumpus.model.map :only (donut-map)]
    [hunt-the-wumpus.model.player :only (player-location player-dead? kill-player)]
    [hunt-the-wumpus.model.report :only (cavern-report game-over-report hazard-report player-report item-report)]))

(defn new-game [& args]
  (let [options (apply hash-map args)]
    {:caverns (or (:caverns options) {})
     :hazards (or (:hazards options) {})
     :items (or (:items options) {})
     :players (or (:players options) {})}))

(defn report [before after player]
  (if-let [game-over-report (seq (game-over-report after player))]
    {:game-over-messages game-over-report}
    {:hazard-messages (hazard-report after player)
     :player-messages (player-report before after player)
     :item-messages (item-report before after player)
     :cavern-messages (cavern-report after player)}))

(defn pick-up-items [game player]
  (let [cavern (player-location game player)
        items (items-in game cavern)]
    (-> game
      (add-items player items)
      (unplace-items cavern))))

(defn mark-dead-player [game player]
  (let [player-loc (player-location game player)
        wumpus-loc (hazard-locations game :wumpus)]
    (if ((set wumpus-loc) player-loc)
      (kill-player game player :wumpus)
      game)))

(defn- update-game [game command-thunk player]
  (-> game
    (command-thunk player)
    (pick-up-items player)
    (mark-dead-player player)))

(defn perform-command [game-ref player command-thunk]
  (dosync
    (try
      (if (not (player-dead? @game-ref player))
        (let [before @game-ref
              after (alter game-ref update-game command-thunk player)]
          (report before after player))
        (throw (Exception. "Your game is over. Go in peace.")))
      (catch Exception e
        ;                (.printStackTrace e)
        {:error (.getMessage e)}))))

(def ^:dynamic *game*
  (ref
    (-> (new-game :caverns (donut-map))
      (place-hazard :wumpus 5)
      (place-item :arrow 3)
      (place-item :arrow 7))))
