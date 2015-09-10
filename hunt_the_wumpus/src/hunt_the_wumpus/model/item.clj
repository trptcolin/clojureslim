(ns hunt-the-wumpus.model.item
  (:use
    [hunt-the-wumpus.model.player :only (player-location)]))

(def items #{:arrow})

(defn item? [thing]
  (not (nil? (some items [(keyword thing)]))))

(defn place-item [game item cavern]
  (update-in game [:items (keyword item)] conj cavern))

(defn add-items [game player items]
  (update-in game [:players player :items] concat items))

(defn items-of [game player]
  (:items (get (:players game) player)))

(defn items-in [game cavern]
  (reduce
    (fn [result [item caverns]]
      (reduce
        (fn [result c]
          (if (= cavern c)
            (conj result item)
            result))
        result
        caverns))
    []
    (:items game)))

(defn unplace-items [game cavern]
  (update-in game [:items]
    (fn [items]
      (into {}
        (reduce
          (fn [items [item caverns]]
            (conj items [item (remove #{cavern} caverns)]))
          []
          items)
        ))))




