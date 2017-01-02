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

(defn start-keyboard-loop [key-map]
  (when-not @state/loop-running?
    (state/loop-running)
    (go-loop []
      (let [key (-> keydown-chan-events <! .-keyCode)]
        (if-let [action (get key-map key)]
          (action)
          (js/console.log key))
        (recur)))))
