(ns showrum.app
  (:require [rum.core :as rum]
            [bide.core :as router]
            [beicon.core :as rxt]
            [potok.core :as ptk]
            [showrum.routes :as routes]
            [showrum.store :as store]
            [showrum.events :as events]
            [showrum.views :as views]))

(defn mount-root
  "Mounts root"
  []
  (rum/mount (views/main store/main) (js/document.getElementById "container")))


(router/start!
  routes/config
  {:default      :showrum/index
   :on-navigate (fn [name params query]
                  (ptk/emit! store/main (events/->RouteMatched name params query)))})


(mount-root)
