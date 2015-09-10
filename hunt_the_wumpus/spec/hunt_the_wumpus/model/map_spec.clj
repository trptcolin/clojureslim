(ns hunt-the-wumpus.model.map-spec
  (:use
    [speclj.core]
    [hunt-the-wumpus.model.map]))

(describe "Map"

  (it "can detect paths from 1 room map"
    (let [map {1 {}}]
      (should= [] (paths-from map 1))))

  (it "can detect paths from center of cross"
    (let [map {1 {:north 2 :east 3 :south 4 :west 5}}]
      (should= [:north :east :south :west] (paths-from map 1))))

  (it "gets the opposite direction"
    (should= :west (opposite-direction :east))
    (should= :east (opposite-direction :west))
    (should= :north (opposite-direction :south))
    (should= :south (opposite-direction :north)))

  (it "adds paths to an empty map"
    (should= {1 {:north 2}
              2 {:south 1}}
      (add-paths {} :start 1 :end 2 :direction :north)))

  (it "adds paths to a populated map"
    (should= {1 {:north 2}
              2 {:south 1 :west 3}
              3 {:east 2}}
      (-> {}
        (add-paths :start 1 :end 2 :direction :north)
        (add-paths :start 2 :end 3 :direction :west))))
  )
