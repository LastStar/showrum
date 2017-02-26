(ns showrum.app
  (:require [rum.core :as rum]
            [showrum.store :as store]
            [showrum.views :as views]))

(defn init []
  (let [store   store/main]
    (rum/mount (views/main store)
               (. js/document (getElementById "container")))))
