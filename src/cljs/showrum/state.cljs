(ns showrum.state
  (:require [rum.core :refer [cursor-in derived-atom]]
            [showrum.db :as db]
            [showrum.parser :as parser]
            [showrum.spec]
            [goog.net.XhrIo :as xhrio]))

(defonce app
  (atom {:db           {:initialized nil}
         :current      {:slide (js/Number.parseInt (.getItem js/localStorage "current-slide"))
                        :deck  (js/Number.parseInt (.getItem js/localStorage "current-deck"))
                        :slides-count 0}
         :loop-running false
         :search {:result 0}}))

(def db-initialized? (cursor-in app [:db :initialized]))
(def current-slide (cursor-in app [:current :slide]))
(def current-slides-count (cursor-in app [:current :slides-count]))
(def current-deck-id (cursor-in app [:current :deck-id]))
(def current-deck (derived-atom [current-deck-id] ::deck
                                (fn [current-deck-id]
                                  (db/deck current-deck-id))))
(def loop-running? (cursor-in app [:loop-running]))
(def searching (cursor-in app [:search :active]))
(def search-term (cursor-in app [:search :term]))
(def search-results (derived-atom [search-term] ::search
                                  (fn [search-term]
                                    (vec (db/search search-term)))))
(def search-result (cursor-in app [:search :result]))

(defn loop-running [] (reset! loop-running? true))

(defn- set-slide
  [slide-no from-storage]
  (let [slide-no (js/Number.parseInt slide-no)]
    (when-not from-storage
      (.setItem js/localStorage "current-slide" slide-no))
    (reset! current-slide slide-no)))

(defn- set-deck-id
  [deck-id]
  (set-slide 1 false)
  (reset! current-deck-id deck-id))

(defn next-slide
  []
  (when (< @current-slide @current-slides-count)
    (set-slide (inc @current-slide) false)))

(defn prev-slide
  []
  (when (> @current-slide 1)
    (set-slide (dec @current-slide) false)))

(defn toggle-search
  []
  (swap! searching not))

(defn search-toggler
  [e]
  (toggle-search))

(defn activate-search-result
  []
  (js/console.log @search-results @search-result
                  (get @search-results @search-result))
  (let [[deck-id _ slide _] (get @search-results @search-result)]
    (set-deck-id deck-id)
    (set-slide slide false)
    (toggle-search)))

(defn set-search-term
  [term]
  (reset! search-result 0)
  (reset! search-term term))

(defn set-result
  [result]
  (when (and (>= result 0)
             (< result (count @search-results)))
    (reset! search-result result)))

(defn next-result
  []
  (-> @search-result inc set-result))

(defn prev-result
  []
  (-> @search-result dec set-result))

(defn search-term-updater
  [e]
  (.preventDefault e)
  (.stopPropagation e)
  (set-search-term (-> e .-target .-value)))

(defn init-local-storage
  []
  (set-deck-id 1)
  (set-slide 1 false))

(defn local-storage-handler
  []
  (aset js/window "onstorage"
        (fn [e]
          (case (.-key e)
            "current-slide" (set-slide (.-newValue e) true)
            "current-deck" (set-deck-id (.-newValue e))))))

(defn db-initialized
  [decks-response]
  (-> decks-response .-target .getResponse parser/parse-decks db/init)
  (init-local-storage)
  (local-storage-handler)
  (reset! db-initialized? true))

(defn db-cleared [] (reset! db-initialized? false))

(defn set-slides-count
  [slides-count]
  (reset! current-slides-count slides-count))

(defn decks
  []
  (db/decks))

(defn init-from-gist [gist-uri]
  (xhrio/send gist-uri db-initialized))
