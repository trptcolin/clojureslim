(ns eg.division
  (:require [chee.coerce :as c]))

(defn new [] (atom {:numerator 0.0 :denominator 0.0}))
(defn quotient [state] (/ (:numerator @state) (:denominator @state)))
(defn set-numerator [state numerator] (swap! state assoc :numerator (c/->double numerator)))
(defn set-denominator [state denominator] (swap! state assoc :denominator (c/->double denominator)))
