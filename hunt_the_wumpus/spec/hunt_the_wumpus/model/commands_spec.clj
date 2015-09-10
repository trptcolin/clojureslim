(ns hunt-the-wumpus.model.commands-spec
  (:use [speclj.core]
        [hunt-the-wumpus.model.commands]))

(defn assert-command-translations [dir versions]
  (doall
    (map
      (fn [direction-version]
        (should= {:command :go :direction dir}
              (translate-command direction-version)))
      versions)))

(describe "Commands"
  (it "translates uppercase commands to a direction"
    (should= :east (translate-direction "E"))
    (should= :west (translate-direction "W"))
    (should= :north (translate-direction "N"))
    (should= :south (translate-direction "S")))

  (it "translates east commands"
    (assert-command-translations
      :east ["E" "e" "east" "East" "Go East" "go east"]))

  (it "translates west commands"
    (assert-command-translations
      :west ["W" "w" "west" "West" "Go West" "go west"]))

  (it "translates south commands"
    (assert-command-translations
      :south ["S" "s" "south" "South" "Go South" "go south"]))

  (it "translates north commands"
    (assert-command-translations
      :north ["N" "n" "north" "North" "Go North" "go north"]))
)

