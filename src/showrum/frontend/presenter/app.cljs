(ns showrum.frontend.presenter.app
  (:require [rum.core :as rum]
            [bide.core :as router]
            [beicon.core :as rxt]
            [potok.core :as ptk]
            [showrum.frontend.presenter.routes :as routes]
            [showrum.frontend.presenter.store :as store]
            [showrum.frontend.presenter.events :as events]
            [showrum.frontend.presenter.views :as views]))

(defonce store (atom nil))

(defn ^:dev/after-load start []
  (rum/mount (views/main @store) (. js/document (getElementById "container"))))

(defn ^:export init []
  (reset! store store/main)
  (router/start!
   routes/config
   {:default     :showrum/index
    :on-navigate (fn [name params query]
                   (ptk/emit! @store (events/->RouteMatched name params query)))})
  (start))
