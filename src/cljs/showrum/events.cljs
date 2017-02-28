(ns showrum.events
  (:require [potok.core :as ptk]
            [beicon.core :as rxt]
            [promesa.core :as p]
            [httpurr.client :as http]
            [httpurr.client.xhr :refer [client]]
            [showrum.parser :as parser]))

(deftype SetGist [gist]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :gist gist)))


(deftype SetGistContent [gist-content]
  ptk/UpdateEvent
  (update [_ state]
    (-> state
        (assoc :gist-content gist-content)
        (assoc :db (parser/parse-decks (:body gist-content))))))

(deftype InitializeGist [gist]
  ptk/WatchEvent
  (watch [_ state stream]
    (let [get-promise (http/get client gist)]
      (rxt/merge
       (rxt/from-promise (p/map ->SetGistContent get-promise))
       (rxt/just (->SetGist gist))))))

(deftype InitializeDecks [decks]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :decks decks)))

(deftype SetCurrentDeck [deck-id]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :slide 1 :deck-id deck-id)))

(deftype SetCurrentSlide [slide-id]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :slide slide-id)))

(deftype SetCurentSlidesCount [count]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :slides-count count)))

(deftype NavigateNextSlide []
  ptk/UpdateEvent
  (update [_ state]
    (if (< (get state :slide)
           (get state :slides-count))
      (update state :slide inc)
      state)))

(deftype NavigatePreviousSlide []
  ptk/UpdateEvent
  (update [_ state]
    (if (> (get state :slide) 1)
      (update state :slide dec)
      state)))

(deftype ToggleSeachPanel []
  ptk/UpdateEvent
  (update [_ state]
    (update state :search/active not)))

(deftype SetSearchTerm [term]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :search/result 0 :search/term term)))

(deftype SetSearchResults [results]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :search/results (vec results))))

(deftype SetActiveSearchResult [index]
  ptk/UpdateEvent
  (update [_ state]
    (if (and (>= index 0)
             (< index (count (get state :search/results))))
      (assoc state :search/result index)
      state)))

(deftype NavigateNextSearchResult []
  ptk/UpdateEvent
  (update [_ state]
    (if (< (get state :search/result)
           (dec (count (get state :search/results))))
      (update state :search/result inc)
      state)))

(deftype NavigatePreviousSearchResult []
  ptk/UpdateEvent
  (update [_ state]
    (if (> (get state :result) 0)
      (update state :search/result dec)
      state)))

(deftype ClearSearchTerm []
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :search/term "" :search/active false)))
