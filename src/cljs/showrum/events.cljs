(ns showrum.events
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [put! chan]]
            [scrum.core :as scrum]
            [goog.events :as evs]
            [goog.events.EventType :as EventType]))

(def keydown-chan-events
  (let [c (chan 1)]
    (evs/listen js/window EventType/KEYDOWN #(put! c %)) c))

(defn start-keyboard-loop [reconciler pres-map search-map]
  (let [loop-running @(scrum/subscription reconciler [:initialized :keyboard-loop])]
    (when-not loop-running
      (scrum/dispatch! reconciler :initialized :keyboard-loop)
      (go-loop []
        (let [event (<! keydown-chan-events)
              key (.-keyCode event)
              loop-running @(scrum/subscription reconciler [:initialized :keyboard-loop])
              active-search @(scrum/subscription reconciler [:search :active])]
          (js/console.log key)
          (if active-search
            (when-let [action (get search-map key)] (action))
            (when-let [action (get pres-map key)] (action)))
          (when loop-running (recur)))))))
