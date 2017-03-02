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
    (assoc state :db/gist gist)))

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
          (assoc :db/decks decks)
          (assoc :db/index rows)
          (assoc :deck/slides-count (count (:deck/slides (first decks))))))))

(deftype ^:private SetCurrentSlide [slide]
  ptk/UpdateEvent
  (update [_ state]
    (if (and (<= slide (:deck/slides-count state))
             (pos? slide))
      (assoc state :slide/current slide)
      state)))

(deftype InitializeGist [gist]
  ptk/WatchEvent
  (watch [_ state stream]
    (let [get-promise (http/get client gist)]
      (rxt/merge
       (rxt/from-promise (p/map ->SetFromGistContent get-promise))
       (rxt/just (->SetGist gist))))))

(deftype SetCurrentDeck [deck]
  ptk/UpdateEvent
  (update [_ state]
    (let [slides-count (count (:deck/slides
                               (some #(and (= deck (:deck/order %)) %) (:db/decks state))))]
      (js/console.log deck slides-count)
      (assoc state :slide/current 1 :deck/current deck
             :deck/slides-count slides-count))))

(deftype NavigateNextSlide []
  ptk/WatchEvent
  (watch [_ state stream]
    (rxt/just (->SetCurrentSlide (inc (:slide/current state))))))

(deftype NavigatePreviousSlide []
  ptk/WatchEvent
  (watch [_ {slide :slide/current} _]
    (rxt/just (->SetCurrentSlide (dec slide)))))

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
    (if (< (:search/result state)
           (dec (count (:search/results state))))
      (update state :search/result inc)
      state)))

(deftype NavigatePreviousSearchResult []
  ptk/UpdateEvent
  (update [_ state]
    (if (> (:search/result state) 0)
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
             (< index (count (:search/results state))))
      (assoc state :search/result index)
      state)))

(deftype ActivateSearchResult [index]
  ptk/WatchEvent
  (watch [_ state stream]
    (let [[deck _ slide _] (nth (:search/results state) index)]
      (rxt/merge
       (rxt/just (->SetCurrentDeck deck))
       (rxt/just (->SetCurrentSlide slide))
       (rxt/just (->ClearSearchTerm))))))

(deftype SetSearchTerm [term]
  ptk/WatchEvent
  (watch [_ state stream]
    (rxt/of
     (->ClearSearchNavigation term)
     (let [tp (re-pattern (str "(?i).*\\b" term ".*"))
           rs (filter #(or (re-matches tp (second %)) (re-matches tp (last %))) (:db/index state))]
       (->SetSearchResults rs)))))

(defn- in-presentation-map
  [key]
  (get {37 (->NavigatePreviousSlide)
        39 (->NavigateNextSlide)
        32 (->NavigateNextSlide)
        83 (->ToggleSearchPanel)}
       key))

(defn- in-search-map
  [key result]
  (get {40 (->NavigateNextSearchResult)
        38 (->NavigatePreviousSearchResult)
        13 (->ActivateSearchResult result)
        27 (->ToggleSearchPanel)}
       key))

(deftype KeyPressed [event]
  ptk/WatchEvent
  (watch [_ {:keys [:db/decks :search/active :search/result]} _]
    (if decks
      (let [key (.-keyCode event)]
        (if active
          (if-let [event (in-search-map key result)]
            (rxt/just event) (rxt/empty))
          (if-let [event (in-presentation-map key)]
            (rxt/just event) (rxt/empty))))
      (rxt/empty))))
