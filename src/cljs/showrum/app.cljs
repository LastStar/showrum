(ns showrum.app
  (:require [rum.core :as rum]
            [scrum.core :as scrum]
            [showrum.dispatchers :refer [reconciler]]
            [showrum.views :refer [main]]))

(defn init []
  (rum/mount (main reconciler)
             (. js/document (getElementById "container"))))
