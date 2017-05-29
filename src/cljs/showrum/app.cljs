(ns showrum.app
  (:require [rum.core :as rum]
            [bide.core :as router]
            [beicon.core :as rxt]
            [potok.core :as ptk]
            [showrum.routes :as routes]
            [showrum.store :as store]
            [showrum.events :as events]
            [showrum.views :as views]))

(defn init []
  (let [store store/main]
    (router/start!
     routes/config
     {:default     :showrum/index
      :on-navigate (fn [name params query]
                     (ptk/emit! store (events/->RouteMatched name params query)))})
    (rum/mount (views/main store) (js/document.getElementById "container"))))
