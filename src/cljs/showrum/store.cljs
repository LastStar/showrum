(ns showrum.store
  (:require [rum.core :as rum]
            [potok.core :as ptk]))

(defonce ^:private initial-state
  {:state {:gist false :db false :keyboard-loop false
           :deck 1 :slide 1 :slides-count 0
           :search/active false :search/term "" :search/results [] :search/result 0}})

(defonce main
  (ptk/store initial-state))

