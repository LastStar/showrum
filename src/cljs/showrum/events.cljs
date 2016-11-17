(ns showrum.events
  (:require [cljs.core.async :refer [put! chan]]
            [goog.events :as events]
            [goog.events.EventType :as EventType]
            [goog.dom :as dom]))

(def keydown-chan-events
  (let [c (chan 1)]
    (events/listen js/window EventType/KEYDOWN #(put! c %))
    c))

