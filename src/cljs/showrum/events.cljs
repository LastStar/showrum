(ns showrum.events
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [put! chan]]
            [goog.events :as evs]
            [goog.events.EventType :as EventType]
            [goog.dom :as dom]
            [showrum.state :as state]))

(def keydown-chan-events
  (let [c (chan 1)]
    (evs/listen js/window EventType/KEYDOWN #(put! c %)) c))

(defn start-keyboard-loop [pres-map search-map]
  (when-not @state/loop-running?
    (state/loop-running)
    (go-loop []
      (let [event (<! keydown-chan-events)
            key (.-keyCode event)]
        (when-let [action (get pres-map key)]
          (when-not @state/searching (action)))
        (when-let [action (get search-map key)]
          (when @state/searching
            (action)))
        (recur)))))
