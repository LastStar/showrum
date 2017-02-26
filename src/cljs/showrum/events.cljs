(ns showrum.events
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [put! chan]]
            [scrum.core :as scrum]
            [goog.events :as evs]
            [goog.events.EventType :as EventType]))

(def ^:private keydown-chan-events
  (let [c (chan 1)]
    (evs/listen js/window EventType/KEYDOWN #(put! c %)) c))

(defn- in-presentation-map
  [reconciler key]
  (js/console.log reconciler key)
  (get {37 #(scrum/dispatch! reconciler :current :prev-slide)
        39 #(scrum/dispatch! reconciler :current :next-slide)
        32 #(scrum/dispatch! reconciler :current :next-slide)
        83 #(scrum/dispatch! reconciler :search :toggle-active)}
       key))

(defn- in-search-map
  [reconciler key]
  (get {40 #(scrum/dispatch! reconciler :search :next-result)
        38 #(scrum/dispatch! reconciler :search :prev-result)
        13 #(let [[deck-id _ slide _]
                  (get @(scrum/subscription reconciler [:search :results])
                       @(scrum/subscription reconciler [:search :result]))]
              (scrum/dispatch! reconciler :current :deck-id deck-id)
              (scrum/dispatch! reconciler :current :slide slide)
              (scrum/dispatch! reconciler :search :toggle-active)
              (scrum/dispatch! reconciler :search :term ""))
        27 #(scrum/dispatch! reconciler :search :toggle-active)}
       key))

(defn start-keyboard-loop [reconciler]
  (let [loop-running @(scrum/subscription reconciler [:initialized :keyboard-loop])]
    (when-not loop-running
      (scrum/dispatch! reconciler :initialized :keyboard-loop)
      (go-loop []
        (let [event (<! keydown-chan-events)
              key (.-keyCode event)
              loop-running @(scrum/subscription reconciler [:initialized :keyboard-loop])
              active-search @(scrum/subscription reconciler [:search :active])]
          (if active-search
            (when-let [action (in-search-map reconciler key)] (action))
            (when-let [action (in-presentation-map reconciler key)] (action)))
          (when loop-running (recur)))))))
