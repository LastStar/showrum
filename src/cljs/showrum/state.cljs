(ns showrum.state)

(defonce current-slide (atom (js/Number.parseInt (.getItem js/localStorage "current-slide"))))
(defonce current-deck (atom (js/Number.parseInt (.getItem js/localStorage "current-deck"))))
(defonce loop-running (atom false))

(defn set-slide
  "Sets new slide number to atom and local storage"
  [slide-no]
  (.setItem js/localStorage "current-slide" slide-no)
  (reset! current-slide slide-no))

(defn set-deck
  "Sets new deck number to atom and local storage"
  [deck-no]
  (.setItem js/localStorage "current-deck" deck-no)
  (reset! current-deck deck-no))

(defn next-slide
  "Increases current slide and saves it to local storaged"
  []
  (set-slide (inc @current-slide)))

(defn prev-slide
  "Decreases current slide"
  []
  (set-slide (dec @current-slide)))

(defonce db-initialized (atom nil))

