(ns showrum.app
  (:require [rum.core :as rum]
            [scrum.core :as scrum]
            [showrum.dispatchers :as dispatchers]
            [showrum.routes :as routes]
            [showrum.views :as views]))

(defn init []
  (let [reconciler dispatchers/reconciler]
    (rum/mount (views/main reconciler (routes/load reconciler))
               (. js/document (getElementById "container")))))
