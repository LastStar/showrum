(ns showrum.frontend.presenter.events
  (:require [potok.core :as ptk]
            [goog.crypt.base64 :as base64]
            [beicon.core :as rxt]
            [rxhttp.browser :as http]
            [bide.core :as router]
            [showrum.frontend.presenter.routes :as routes]
            [showrum.parser :as parser])
  (:import goog.events.EventType))

(declare ->StartListenKeys)

(defn- look-up [deck decks]
  (some #(and (= deck (:deck/order %)) %) decks))

(defrecord ^:private NavigateUrl []
  ptk/EffectEvent
  (effect [_ {deck :deck/current slide :slide/current gist :db/gist} _]
    (when (and (satisfies? router/IRouter routes/config)
               (and deck slide gist))
      (router/navigate! routes/config
                        :showrum/presentation
                        {:deck deck :slide slide
                         :gist (base64/encodeString gist)}))))

(deftype ^:private SetFromGistContent [gist-response]
  ptk/UpdateEvent
  (update [_ {deck :deck/current :as state}]
    (if (= 200 (:status gist-response))
      (let [decks  (parser/parse-decks (:body gist-response))
            row-fn (fn [{:deck/keys [order title slides]}]
                     (map (fn [{so :slide/order st :slide/title}] [order title so st (count slides)])
                          slides))
            rows   (apply concat (map row-fn decks))
            slides-count (count (:deck/slides (look-up deck decks)))]
        (assoc state :db/decks decks :db/index rows :deck/slides-count slides-count))
      (assoc state :db/error "XHR error" :db/gist nil)))
  ptk/WatchEvent
  (watch [_ state _]
    (if gist-response
      (rxt/of (->NavigateUrl)
              (->StartListenKeys))
      (rxt/empty))))

(defrecord InitializeGist [gist]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :db/gist gist :deck/current 1 :slide/current 1))
  ptk/WatchEvent
  (watch [_ state _]
    (rxt/map ->SetFromGistContent
             (http/send! {:method  :get
                          :url     gist}))))

(deftype ReloadPresentation []
  ptk/WatchEvent
  (watch [_ {gist :db/gist} _]
    (rxt/map ->SetFromGistContent
             (http/send! {:method  :get
                          :url     gist}))))

(defrecord ^:private SetCurrentSlide [slide]
  ptk/UpdateEvent
  (update [_ state]
    (if (<= 1 slide (:deck/slides-count state))
      (assoc state :slide/current slide)
      state))
  ptk/WatchEvent
  (watch [_ state _]
    (rxt/just (->NavigateUrl))))

(deftype NavigateNextSlide []
  ptk/WatchEvent
  (watch [_ {slide :slide/current} _]
    (rxt/just (->SetCurrentSlide (inc slide)))))

(deftype NavigatePreviousSlide []
  ptk/WatchEvent
  (watch [_ {slide :slide/current} _]
    (rxt/just (->SetCurrentSlide (dec slide)))))

(defrecord ^:private SetCurrentDeck [deck]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :deck/current deck))
  ptk/WatchEvent
  (watch [_ _ _]
    (rxt/just (->NavigateUrl))))

(defrecord ^:private SetSlidesCount [count]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :deck/slides-count count)))

(defrecord InitDeck [deck]
  ptk/WatchEvent
  (watch [_ {decks :db/decks} _]
    (if (<= 1 deck (count decks))
      (let [sc (count (:deck/slides (look-up deck decks)))]
        (rxt/of (->SetCurrentDeck deck)
                (->SetCurrentSlide 1)
                (->SetSlidesCount sc)))
      (rxt/empty))))

(deftype ^:private NavigateNextDeck []
  ptk/WatchEvent
  (watch [_ {deck :deck/current} _]
    (rxt/just (->InitDeck (inc deck)))))

(deftype ^:private NavigatePreviousDeck []
  ptk/WatchEvent
  (watch [_ {deck :deck/current} _]
    (rxt/just (->InitDeck (dec deck)))))

(deftype ToggleSearchPanel []
  ptk/UpdateEvent
  (update [_ state]
    (js/console.log "Togggling")
    (update state :search/active not)))

(defrecord ^:private InitSearchNavigation [term]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :search/result 0 :search/term term)))

(defrecord SetActiveSearchResult [index]
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

(defrecord ^:private ClearSearchTerm []
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :search/term "" :search/active false)))

(deftype ActivateSearchResult [index]
  ptk/WatchEvent
  (watch [_ {results :search/results} stream]
    (let [[deck _ slide _ slides-count] (nth results index)]
      (rxt/of
       (->SetCurrentSlide slide)
       (->SetCurrentDeck deck)
       (->SetSlidesCount slides-count)
       (->ClearSearchTerm)))))

(defrecord ^:private SetSearchResults [results]
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :search/results (vec results)
           :search/results-count (count results))))

(deftype SetSearchTerm [term]
  ptk/WatchEvent
  (watch [_ {index :db/index} stream]
    (let [tp (re-pattern (str "(?i).*\\b" term ".*"))
          rs (filter (fn [[_ dt _ st _]]
                       (or (re-matches tp dt)
                           (re-matches tp st))) index)]
      (rxt/of
       (->InitSearchNavigation term)
       (->SetSearchResults rs)))))

(defrecord SetHover []
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :navigation/hovered true)))

(defn- set-hover? [i] (instance? SetHover i))

(defrecord RemoveHover []
  ptk/UpdateEvent
  (update [_ state]
    (assoc state :navigation/hovered false)))

(defrecord SetLeft []
  ptk/WatchEvent
  (watch [_ state stream]
    (let [stopper (->> (rxt/filter #(set-hover? %) stream)
                       (rxt/take 1))]
      (->>
       (rxt/just (->RemoveHover))
       (rxt/delay 2000)
       (rxt/take-until stopper)))))


(defn- in-presentation-map
  [key]
  (get {37 (->NavigatePreviousSlide)
        39 (->NavigateNextSlide)
        38 (->NavigatePreviousDeck)
        40 (->NavigateNextDeck)
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
    (if-let [event (if active
                     (in-search-map key result)
                     (in-presentation-map key))]
      (rxt/just event)
      (rxt/empty))))

(defrecord StartListenKeys []
  ptk/WatchEvent
  (watch [_ state _]
    (let [interval     750
          event-stream (rxt/from-event js/document EventType.KEYDOWN)
          key-stream   (rxt/throttle interval event-stream)]
      (rxt/map #(->KeyPressed (.-keyCode %)) key-stream))))

(deftype RouteMatched [name params query]
  ptk/UpdateEvent
  (update [_ state]
    (let [deck  (js/parseInt (:deck params))
          slide (js/parseInt (:slide params))]
      (assoc state :slide/current slide :deck/current deck)))
  ptk/WatchEvent
  (watch [_ {gist :db/gist} _]
    (case name
      :showrum/presentation
      (let [gist-from-url (base64/decodeString
                           (js/decodeURIComponent (:gist params)))]
        (if-not (= gist-from-url gist)
          (rxt/just (->InitializeGist gist-from-url))
          (rxt/empty)))
      :showrum/index
      (rxt/empty))))
