(ns hunt-the-wumpus.model.commands
  (:require [clojure.string :as string]))

(defn translate-direction [command]
  (some
    (fn [[k vs]]
      (if (some #{command} vs)
        k
        nil))
    {:east ["E" "e" "east" "East" "Go East" "go east"]
     :west ["W" "w" "west" "West" "Go West" "go west"]
     :south ["S" "s" "south" "South" "Go South" "go south"]
     :north ["N" "n" "north" "North" "Go North" "go north"]}))

(defn rest? [command]
  (some #{command} ["rest" "Rest" "r" "R"]))

(defn translate-command [raw-command]
  (if-let [direction (translate-direction raw-command)]
    {:command :go :direction direction}
    (if (rest? raw-command)
      {:command :rest}
      (str "I don't know how to " raw-command "."))))

