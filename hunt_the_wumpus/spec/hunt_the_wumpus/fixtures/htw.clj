(ns hunt-the-wumpus.fixtures.htw
  (:require [hunt-the-wumpus.fixtures.context :refer [game-ref last-report]]
            [hunt-the-wumpus.model.commands :refer [translate-direction translate-command]]
            [hunt-the-wumpus.model.game :as game :refer [perform-command]]
            [hunt-the-wumpus.model.map :refer [add-paths opposite-direction]]
            [hunt-the-wumpus.model.player :refer [place-player player-location move-player-in-direction]]
            [hunt-the-wumpus.model.item :refer [add-items place-item item? items-in]]
            [hunt-the-wumpus.model.hazard :refer [place-hazard hazard?]]))

(defn clear-map [this]
  )

(defn put-in-cavern [state thing location]
  (dosync
    (cond
      (hazard? thing) (alter game-ref place-hazard thing location)
      (item? thing) (alter game-ref place-item thing location)
      :else (alter game-ref place-player thing location))))

(defn- command-spec->thunk [command-spec player]
  (cond
    (= :go (:command command-spec))
    (fn [game player] (move-player-in-direction game player (:direction command-spec)))
    (= :rest (:command command-spec))
    (fn [game player] game)
    :else
    (fn [game player] (throw (Exception. command-spec)))))

(defn enter-command-for [this raw-command player]
  (println "@game-ref before: " @game-ref)
  (let [command-spec (translate-command raw-command)
        thunk (command-spec->thunk command-spec player)
        report (perform-command game-ref player thunk)]
    (reset! last-report report)
    (println "@last-report: " @last-report)
    (println "@game-ref after: " @game-ref)))

(defn error-message [this]
  (:error @last-report))

(defn cavern-has [this n player]
  (= n (player-location @game-ref player)))

(defn message-was-printed [this message]
  (println "@last-report: " @last-report)
  (println "(apply concat (vals @last-report)): " (apply concat (vals @last-report)))
  (boolean
    (some #{message}
          (apply concat (vals @last-report)))))

(defn freeze-wumpus [this v]
  )

(defn set-quiver-to-for [this quiver player]
  (dosync
    (loop [quiver (Integer/parseInt quiver) game @game-ref]
      (when (> quiver 0)
        (recur (dec quiver) (add-items game player [:arrow]))))))

(defn arrows-in-cavern [this cavern]
  (or
    (:arrow (frequencies (items-in @game-ref cavern)))
    0))


(defn arrows-in-quiver [this]
  )

(defn game-terminated [this]
  )

(defn new-game [this]
  )

(defn check-random-wumpus-movement []
  )

(defn set-cavern [this cavern]
  )

(defn check-random-bat-transport []
  )
