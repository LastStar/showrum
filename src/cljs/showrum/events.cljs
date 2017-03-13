(ns showrum.events
  (:require [potok.core :as ptk]
            [beicon.core :as rxt]
            [promesa.core :as p]
            [httpurr.client :as http]
            [httpurr.client.xhr :refer [client]]
            [httpurr.status :as status]
            [showrum.parser :as parser]))

(deftype ^:private SetGist [gist]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :db/gist gist)))

(deftype ^:private SetFromGistContent [gist-response]
  ptk/UpdateEvent
  (update [_ state]
    (if (status/success? gist-response)
      (let [decks  (parser/parse-decks (:body gist-response))
            flatfn (fn [d]
                     (map #(vector (:deck/order d) (:deck/title d)
                                   (:slide/order %) (:slide/title %))
                          (:deck/slides d)))
            rows   (apply concat
                          (map flatfn decks))]
        (assoc state :db/decks decks
               :db/index rows
               :deck/current 1
               :slide/current 1
               :deck/slides-count (count (:deck/slides (first decks)))))
      (assoc state :db/error "XHR error" :db/gist nil))))

(deftype InitializeGist [gist]
  ptk/WatchEvent
  (watch [_ state _]
    (rxt/merge
     (rxt/from-promise
      (p/then (http/get client gist) ->SetFromGistContent))
     (rxt/just (->SetGist gist)))))

(deftype ReloadPresentation []
  ptk/WatchEvent
  (watch [_ {gist :db/gist} _]
    (js/console.log gist)
    (rxt/just (->InitializeGist gist))))

(deftype SetCurrentDeck [deck]
  ptk/UpdateEvent
  (update [_ {decks :db/decks :as state}]
    (let [sc (count (:deck/slides
                     (some #(and (= deck (:deck/order %)) %) decks)))]
      (assoc state :slide/current 1 :deck/current deck :deck/slides-count sc))))

(deftype ^:private SetCurrentSlide [slide]
  ptk/UpdateEvent
  (update [_ state]
    (if (<= 1 slide (:deck/slides-count state))
      (assoc state :slide/current slide)
      state)))

(deftype NavigateNextSlide []
  ptk/WatchEvent
  (watch [_ {slide :slide/current} _]
    (rxt/just (->SetCurrentSlide (inc slide)))))

(deftype NavigatePreviousSlide []
  ptk/WatchEvent
  (watch [_ {slide :slide/current} _]
    (rxt/just (->SetCurrentSlide (dec slide)))))

(deftype ToggleSearchPanel []
  ptk/UpdateEvent
  (update [_ state]
    (update state :search/active not)))

(deftype ^:private ClearSearchNavigation [term]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :search/result 0 :search/term term)))

(deftype SetActiveSearchResult [index]
  ptk/UpdateEvent
  (update [_ {count :search/results-count :as state}]
    (if (<= 0 index (dec count))
      (assoc state :search/result index)
      state)))

(deftype ^:private NavigateNextSearchResult []
  ptk/WatchEvent
  (watch [_ {result :search/result} _]
    (rxt/just (->SetActiveSearchResult (inc result)))))

(deftype ^:private NavigatePreviousSearchResult []
  ptk/WatchEvent
  (watch [_ {result :search/result} _]
    (rxt/just (->SetActiveSearchResult (dec result)))))

(deftype ^:private ClearSearchTerm []
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :search/term "" :search/active false)))

(deftype ActivateSearchResult [index]
  ptk/WatchEvent
  (watch [_ {results :search/results} stream]
    (let [[deck _ slide _] (nth results index)]
      (rxt/of
       (->SetCurrentDeck deck)
       (->SetCurrentSlide slide)
       (->ClearSearchTerm)))))

(deftype ^:private SetSearchResults [results]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :search/results (vec results))))

(deftype ^:private SetSearchResultsCount [count]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :search/results-count count)))

(deftype SetSearchTerm [term]
  ptk/WatchEvent
  (watch [_ {index :db/index} stream]
    (let [tp (re-pattern (str "(?i).*\\b" term ".*"))
          rs (filter #(or (re-matches tp (second %))
                          (re-matches tp (last %))) index)]
      (rxt/of
       (->ClearSearchNavigation term)
       (->SetSearchResultsCount (count rs))
       (->SetSearchResults rs)))))

(defn- in-presentation-map
  [key]
  (get {37 (->NavigatePreviousSlide)
        39 (->NavigateNextSlide)
        32 (->NavigateNextSlide)
        13 (->NavigateNextSlide)
        82 (->ReloadPresentation)
        83 (->ToggleSearchPanel)}
       key))

(defn- in-search-map
  [key result]
  (get {40 (->NavigateNextSearchResult)
        38 (->NavigatePreviousSearchResult)
        13 (->ActivateSearchResult result)
        27 (->ToggleSearchPanel)}
       key))

(deftype KeyPressed [key]
  ptk/WatchEvent
  (watch [_ {:keys [db/decks search/active search/result]} _]
    (let [event (if active
                  (in-search-map key result)
                  (in-presentation-map key))]
      (if event (rxt/just event) (rxt/empty)))))
