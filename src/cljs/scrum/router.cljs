(ns scrum.router
  (:require [bide.core :as r]
            [pushy.core :as p]))

(defn start!
  "Initialize router"
  [on-set-page routes]
  (let [history (p/pushy on-set-page (partial r/match (r/router routes)))]
    (p/start! history)
    history))

(defn stop!
  "Stop router"
  [history]
  (p/stop! history))
