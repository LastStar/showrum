(ns showrum.store
  (:require [rum.core :as rum]
            [potok.core :as ptk]))

(defonce ^:private initial-state
  {:state {:db/gist false :db/decks false
           :deck/current 1 :slide/current 1 :deck/slides-count 0
           :search/active false :search/term "" :search/results [] :search/result 0}})

(defonce main
  (ptk/store initial-state))

