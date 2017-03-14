(ns showrum.app
  (:require [rum.core :as rum]
            [potok.core :as ptk]
            [goog.events :as events]
            [bide.core :as router]
            [showrum.store :as store]
            [showrum.events :refer [->KeyPressed ->RouteMatched]]
            [showrum.views :as views])
  (:import goog.events.EventType))

(def routes
  (router/router [["/presentation/:deck/:slide" :showrum/presentation]]))

(defn init []
  (let [store store/main]
    (router/start! routes
                   {:default     :showrum/index
                    :on-navigate (fn [name params query]
                                   (ptk/emit! store (->RouteMatched name params query)))})
    (events/removeAll js/document EventType.KEYDOWN)
    (events/listen js/document
                   EventType.KEYDOWN
                   #(ptk/emit! store (->KeyPressed (.-keyCode %))))
    (rum/mount (views/main store)
               (js/document.getElementById "container"))))
