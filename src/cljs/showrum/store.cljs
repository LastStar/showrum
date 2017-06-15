(ns showrum.store
  (:require [potok.core :as ptk]))

(defonce ^:private initial-state
  {:state {:db/gist false :db/decks false
           :deck/current false :slide/current false :deck/slides-count false
           :navigation/hovered false
           :search/active false :search/term "" :search/results [] :search/result 0}
   :on-error #(js/console.error %)})

(defonce main
  (ptk/store initial-state))

