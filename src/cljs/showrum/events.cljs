(ns showrum.events
  (:require [potok.core :as ptk]
            [beicon.core :as rxt]
            [promesa.core :as p]
            [httpurr.client :as http]
            [httpurr.client.xhr :refer [client]]
            [showrum.parser :as parser]))

(deftype ^:private SetGist [gist]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :gist gist)))


(deftype ^:private SetFromGistContent [gist-content]
  ptk/UpdateEvent
  (update [_ state]
    (let [decks  (parser/parse-decks (:body gist-content))
          flatfn (fn [d]
                   (map #(vector (:deck/order d) (:deck/title d)
                                 (:slide/order %) (:slide/title %))
                        (:deck/slides d)))
          rows   (apply concat
                        (map flatfn decks))]
      (-> state
          (assoc :db decks)
          (assoc :rows rows)
          (assoc :slides-count (count (:deck/slides (first decks))))))))

(deftype InitializeGist [gist]
  ptk/WatchEvent
  (watch [_ state stream]
    (let [get-promise (http/get client gist)]
      (rxt/merge
       (rxt/from-promise (p/map ->SetFromGistContent get-promise))
       (rxt/just (->SetGist gist))))))

;; Set current slides count
(deftype SetCurrentDeck [deck]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :slide 1 :deck deck
           :slides-count (count (:deck/slides (some #(and (= deck (:deck/order %)) %) (:db state)))))))

(deftype SetCurentSlidesCount [count]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :slides-count count)))

(deftype SetCurrentSlide [slide]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :slide slide)))

;; Move logic to ^^^^
(deftype NavigateNextSlide []
  ptk/WatchEvent
  (watch [_ state stream]
    (let [slide (get state :slide)
          new-slide (if (< (get state :slide) (get state :slides-count))
                      (inc slide) slide)]
      (rxt/just (->SetCurrentSlide new-slide)))))

(deftype NavigatePreviousSlide []
  ptk/WatchEvent
  (watch [_ state stream]
    (let [slide (get state :slide)
          new-slide (if (> slide 1) (dec slide) slide)]
      (rxt/just (->SetCurrentSlide new-slide)))))
;; ^^^

(deftype ToggleSearchPanel []
  ptk/UpdateEvent
  (update [_ state]
    (update state :search/active not)))

(deftype ClearSearchNavigation [term]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :search/result 0 :search/term term)))

(deftype SetSearchResults [results]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :search/results (vec results))))

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

(deftype SetActiveSearchResult [index]
  ptk/UpdateEvent
  (update [_ state]
    (if (and (>= index 0)
             (< index (count (get state :search/results))))
      (assoc state :search/result index)
      state)))

(deftype ActivateSearchResult [index]
  ptk/WatchEvent
  (watch [_ state stream]
    (let [[deck _ slide] (nth (:search/results state) index)]
      (rxt/merge
       (rxt/just (SetCurrentDeck. deck))
       (rxt/just (SetCurrentSlide. slide))
       (rxt/just (ClearSearchTerm.))))))

(deftype SetSearchTerm [term]
  ptk/WatchEvent
  (watch [_ state stream]
    (rxt/of
     (ClearSearchNavigation. term)
     (let [tp (re-pattern (str "(?i).*\\b" term ".*"))
           rs (filter #(or (re-matches tp (second %)) (re-matches tp (last %))) (:rows state))]
       (SetSearchResults. rs)))))
