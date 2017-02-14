(ns showrum.events
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [put! chan]]
            [scrum.core :as scrum]
            [goog.events :as evs]
            [goog.events.EventType :as EventType]
            [goog.dom :as dom]
            [showrum.dispatchers :refer [reconciler]]))

(def keydown-chan-events
  (let [c (chan 1)]
    (evs/listen js/window EventType/KEYDOWN #(put! c %)) c))

(defn start-keyboard-loop [pres-map search-map]
  (when-not @(scrum/subscription reconciler [:initialized :keyboard-loop])
    (scrum/dispatch! reconciler :initialized :keyboard-loop)
    (go-loop []
      (let [event (<! keydown-chan-events)
            key (.-keyCode event)
            active-search (scrum/subscription reconciler [:search :active])]
        (js/console.log key)
        (when-let [action (get pres-map key)]
          (when-not @active-search (action)))
        (when-let [action (get search-map key)]
          (when @active-search (action)))
        (recur)))))
