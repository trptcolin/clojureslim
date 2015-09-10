(ns hunt-the-wumpus.model.map)

(def opposite-direction
  {:east :west
   :west :east
   :north :south
   :south :north})

(defn add-paths [caverns & {:keys [start end direction]}]
  (-> caverns
      (update-in [start] assoc direction end)
      (update-in [end] assoc (opposite-direction direction) start)))

(defn paths-from [map room]
  (or
    (keys (get map room))
    []))

(defn possible-paths [game cavern]
  (get (:caverns game) cavern))

(defn donut-map []
  {1 {:east 2 :south 8}
   2 {:east 3 :west 1}
   3 {:south 4 :west 2}
   4 {:south 5 :north 3}
   5 {:west 6 :north 4}
   6 {:west 7 :east 5}
   7 {:north 8 :east 6}
   8 {:north 1 :south 7}})