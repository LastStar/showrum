(ns showrum.app
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [showrum.db :as db]
            [showrum.events :as events]))

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

(rum/defc slider < rum/reactive [slides]
  [:div.deck
   {:style {:width     (str (count slides) "00vw")
            :transform (str "translateX(-" (dec (rum/react current-slide)) "00vw)")}}
   (for [{id    :db/id
          order :slide/order
          type  :slide/type
          title :slide/title
          text  :slide/text
          image :slide/image
          :as   slide} (sort-by :slide/order slides)]
     (case type
       :type/main-header
       [:div.slide.main.header
        {:key id}
        [:h1.title title]]
       :type/header
       [:div.slide.header
        {:key id}
        [:h1.title title]]
       :type/bullets
       [:div.slide.bullets
        {:key id}
        [:h1.title title]
        [:ul
         (for [item (:slide/bullets slide)]
           [:li
            item])]]
       :type/text
       [:div.slide.text
        {:key id}
        [:h1.title title]
        [:p text]]
       :type/image
       [:div.slide.image
        {:key id}
        [:h1.title title]
        [:div
         [:img
          {:src (last (re-matches #".*\((.*)\)" image))}]]]))])

(rum/defc slides-counter < rum/reactive
  [slides-count]
  [:div.counter (str (rum/react current-slide) " of " slides-count)])

(rum/defcs navigation <
  rum/reactive
  (rum/local false ::hovered)
  (rum/local nil ::timer)
  [state slides decks]
  (let [hovered      (::hovered state)
        timer        (::timer state)
        slides-count (count slides)
        hover-class  (or (and (or @hovered
                                  (= (rum/react current-slide) 1)
                                  (= (rum/react current-slide) slides-count)) "hovered") "")]
    [:div.navigation
     {:class          hover-class
      :on-mouse-enter #(reset! hovered true)
      :on-mouse-leave (fn [e]
                        (when @timer
                          (.clearTimeout js/window @timer))
                        (reset! timer (.setTimeout js/window #(reset! hovered false) 2000)))}
     [:nav.decks
      (for [[id _ title] decks]
        [:div
         {:key id}
         (mdl/button
          {:mdl      [:ripple]
           :disabled (= (rum/react current-deck) id)
           :on-click (fn [e]
                       (set-slide 1)
                       (set-deck id))}
          title)])]
     [:nav.slides
      (let [active (and (> (rum/react current-slide) 1) :active)]
        (mdl/button
         {:mdl      [:fab :mini-fab]
          :on-click (when active #(prev-slide))
          :disabled    (not active)}
         (mdl/icon "navigate_before")))
      (let [active (and (< (rum/react current-slide) slides-count) :active)]
        (mdl/button
         {:mdl      [:fab :mini-fab]
          :on-click (when active #(next-slide))
          :disabled    (not active)}
         (mdl/icon "navigate_next")))]
     (slides-counter slides-count)]))

(rum/defc footer [deck]
  (let [{author :deck/author date :deck/date place :deck/place} deck]
    [:footer
     [:div author]
     [:div date]
     [:div place]]))

(rum/defc notes < rum/reactive [slides]
  [:div.page
   [:div.navigation
    (slides-counter (count slides))]
   [:div.notes
    {:style {:width     (str (count slides) "00vw")
             :transform (str "translateX(-" (dec (rum/react current-slide)) "00vw)")}}
    (for [slide (sort-by :slide/order slides)]
      (if-let [notes (:slide/notes slide)]
        ^{:key (:db/id slide)}
        [:div.note
         notes]
        ^{:key (:db/id slide)}
        [:div.note
         "No notes for this slide"]))]])

(rum/defc present [decks deck slides]
  [:div.page
   (navigation slides decks)
   (slider slides)
   (footer deck)])

(rum/defc gist-form []
  [:div.gist
   (mdl/grid
    (mdl/cell {:mdl [:2]})
    (mdl/cell {:mdl [:8]}
              [:h4 "No decks loaded. Please add uri for the gist."]
              [:form
               {:on-submit (fn [e]
                             (.stopPropagation e)
                             (.preventDefault e)
                             (db/init-from-gist (-> e .-target (aget "gist") .-value)))}
               [:div
                (mdl/textfield
                 {:style {:width "50rem"}}
                 (mdl/textfield-input {:type "text" :id "gist"})
                 (mdl/textfield-label {:for "gist"} "Gist URI"))]
               [:div
                (mdl/button {:mdl [:raised :ripple]} "Parse")]])
    (mdl/cell {:mdl [:2]}))])

(rum/defc page < rum/reactive []
  (if (rum/react db/initialized)
    (let [decks (db/decks)]
      (let [deck (db/deck (rum/react current-deck))
            slides (:deck/slides deck)
            hash (-> js/document .-location .-hash)]
        (if (= hash "#notes")
          (notes slides)
          (present decks deck slides))))
    (gist-form)))

(defn keyboard-loop
  "Starts keyboard loop"
  []
  (go-loop []
    (let [key (<! events/keydown-chan-events)]
      (let [slides-count (count (:deck/slides (db/deck @current-deck)))]
        (case (.-keyCode key)
          37 (when (> @current-slide 1) (prev-slide))
          39 (when (< @current-slide slides-count) (next-slide))
          32 (when (< @current-slide slides-count) (next-slide))
          (js/console.log key)))
      (recur))))

(defn local-storage-handler
  "Sets callback for local storage"
  []
  (aset js/window "onstorage"
        (fn [e]
          (case (.-key e)
            "current-slide" (set-slide (.-newValue e))
            "current-deck" (set-deck (.-newValue e))))))

(defn init []
  (db/init-local-storage)
  (when-not @loop-running
    (keyboard-loop)
    (swap! loop-running not))
  (local-storage-handler)
  (rum/mount (page) (. js/document (getElementById "container"))))
