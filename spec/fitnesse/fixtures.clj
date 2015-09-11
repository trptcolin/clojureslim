(ns fitnesse.fixtures
  (:import (fitnesse.fixtures SetUp)))

(defn new-set-up [property] (SetUp. property))