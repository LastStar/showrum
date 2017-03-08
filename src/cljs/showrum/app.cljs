(ns showrum.app
  (:require [rum.core :as rum]
            [potok.core :as ptk]
            [goog.events :as events]
            [showrum.store :as store]
            [showrum.events :refer [->KeyPressed]]
            [showrum.views :as views])
  (:import goog.events.EventType))

(defn init []
  (let [store   store/main]
    (events/removeAll js/document EventType.KEYDOWN)
    (events/listen js/document
                   EventType.KEYDOWN
                   #(ptk/emit! store (->KeyPressed (.-keyCode %)))) 
    (rum/mount (views/main store)
               (js/document.getElementById "container"))))
