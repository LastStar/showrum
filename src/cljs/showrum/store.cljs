(ns showrum.store
  (:require [rum.core :as rum]
            [potok.core :as ptk]))

(defonce ^:private initial-state
  {:state {:gist false :db false :keyboard-loop false
           :deck-id 1 :slide 1 :slides-count 0
           :active false :term "" :results [] :result 0}})

(defonce main
  (ptk/store initial-state))

