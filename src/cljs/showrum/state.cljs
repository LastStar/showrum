(ns showrum.state
  (:require [rum.core :refer [cursor-in]]))

(defonce app
  (atom {:db           {:initialized nil}
         :current      {:slide (js/Number.parseInt (.getItem js/localStorage "current-slide"))
                        :deck  (js/Number.parseInt (.getItem js/localStorage "current-deck"))}
         :loop-running false}))

(def db-initialized? (cursor-in app [:db :initialized]))
(def current-slide (cursor-in app [:current :slide]))
(def current-deck (cursor-in app [:current :deck]))
(def loop-running? (cursor-in app [:loop-running]))

(defn- set-slide
  [slide-no from-storage]
  (let [slide-no (js/Number.parseInt slide-no)]
    (when-not from-storage
      (.setItem js/localStorage "current-slide" slide-no))
    (reset! current-slide slide-no)))

(defn- set-deck
  [deck-no]
  (.setItem js/localStorage "current-deck" deck-no)
  (set-slide 1 false)
  (reset! current-deck deck-no))

(defn next-slide
  []
  (set-slide (inc @current-slide) false))

(defn prev-slide
  []
  (set-slide (dec @current-slide) false))

(defn init-local-storage
  []
  (set-deck 1)
  (set-slide 1 false))

(defn local-storage-handler
  []
  (aset js/window "onstorage"
        (fn [e]
          (case (.-key e)
            "current-slide" (set-slide (.-newValue e) true)
            "current-deck" (set-deck (.-newValue e))))))

(defn db-initialized []
  (init-local-storage)
  (local-storage-handler)
  (reset! db-initialized? true))

(defn db-cleared [] (reset! db-initialized? false))

(defn loop-running [] (swap! loop-running? not))
