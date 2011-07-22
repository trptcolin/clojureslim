(ns clojureslim
  (:gen-class)
  (:import [fitnesse.slim JavaSlimFactory
                          SlimServer
                          SlimService]))

(defn -main [& args]
  (let [port (Integer/parseInt (first args))
        slim-factory (JavaSlimFactory.)
        slim-server (.getSlimServer slim-factory true)]
    (SlimService. port slim-server)))
