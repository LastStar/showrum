(ns showrum.routes
  (:require [scrum.core :as scrum]
            [scrum.router :as router]))

(def routes
  [["/" :index]
   ["/presentation" :presentation]])

(defn load
  [reconciler]
  (router/start!
   #(scrum/dispatch! reconciler :router :push %)
   routes))
