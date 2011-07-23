(ns clojureslim.clj-test)

(defprotocol CljTestProtocol
  (somethingHappened [this]))

(def something-happened? (atom false))

(defrecord CljTest []
  Runnable
  (run [this]
    (swap! something-happened? #(not %)))

  CljTestProtocol
  (somethingHappened [this]
    @something-happened?))

