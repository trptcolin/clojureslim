(ns hunt-the-wumpus.fixtures
  (:require [hunt-the-wumpus.model.commands :refer [translate-direction translate-command]]
            [hunt-the-wumpus.model.game :as game :refer [perform-command]]
            [hunt-the-wumpus.model.map :refer [add-paths opposite-direction]]
            [hunt-the-wumpus.model.player :refer [place-player player-location move-player-in-direction]]
            [hunt-the-wumpus.model.item :refer [add-items place-item item? items-in]]
            [hunt-the-wumpus.model.hazard :refer [place-hazard hazard?]]))

(def game-ref (ref (game/new-game)))
(def last-report (atom nil))

(defprotocol SlimTable
  (execute [this])
  (end-table [this]))

(defprotocol AddPath
  (set-start [this start])
  (set-end [this start])
  (set-direction [this start]))

(defrecord MapMaker [map start end direction]
  AddPath
  (set-start [this value] (reset! start value))
  (set-end [this value] (reset! end value))
  (set-direction [this value] (reset! direction value))
  SlimTable
  (execute [this]
    (swap! map add-paths
           :start @start
           :end @end
           :direction (translate-direction @direction)))
  (end-table [this]
    (dosync
      (alter game-ref assoc :caverns @map))))

(defn make-map []
  (MapMaker. (atom {}) (atom nil) (atom nil) (atom nil)))

(defn clear-map [this]
  )

(defn put-in-cavern [this thing location]
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
