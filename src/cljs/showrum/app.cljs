(ns showrum.app
  (:require [rum.core :as rum]
            [bide.core :as router]
            [beicon.core :as rxt]
            [potok.core :as ptk]
            [showrum.routes :as routes]
            [showrum.store :as store]
            [showrum.events :refer [->KeyPressed ->RouteMatched]]
            [showrum.views :as views])
  (:import goog.events.EventType))

(defn init []
  (let [store store/main
        key-stream (rxt/from-event js/document EventType.KEYDOWN)]
    (router/start!
     routes/config
     {:default     :showrum/index
      :on-navigate (fn [name params query]
                     (ptk/emit! store (->RouteMatched name params query)))})
    (rxt/on-value key-stream #(ptk/emit! store (->KeyPressed (.-keyCode %))))
    (rum/mount (views/main store)
               (js/document.getElementById "container"))))
