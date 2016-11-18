(ns showrum.app
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [showrum.db :as db]
            [showrum.events :as events]))

(defonce current-slide (atom 1))

(defonce current-deck (atom 1))

(defn next-slide
  "Increases current slide"
  []
  (swap! current-slide inc))

(defn prev-slide
  "Decreases current slide"
  []
  (swap! current-slide dec))

(rum/defc slider < rum/reactive [slides]
  [:div.deck
   {:style {:width     (str (count slides) "00vw")
            :transform (str "translateX(-" (dec (rum/react current-slide)) "00vw)")}}
   (for [{id    :db/id
          order :slide/order
          type  :slide/type
          title :slide/title
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
            item])]]))])

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
                      (reset! current-slide 1)
                      (reset! current-deck id))}
         title)])]
     [:div.counter (str (rum/react current-slide) " of " slides-count)]
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
         (mdl/icon "navigate_next")))]]))

(rum/defc footer [deck]
  (let [{author :deck/author date :deck/date place :deck/place} deck]
    [:footer
     [:div author]
     [:div date]
     [:div place]]))

(rum/defc page < rum/reactive []
  (let [decks (db/decks)
        deck (db/deck (rum/react current-deck))
        slides (:deck/slides deck)]
    [:div.page
     (navigation slides decks)
     (slider slides)
     (footer deck)]))

(defn init []
  (db/init)
  (go-loop []
    (let [key (<! events/keydown-chan-events)]
      (let [slides-count (count (:deck/slides (db/deck @current-deck)))]
        (case (.-keyCode key)
          37 (when (> @current-slide 1) (prev-slide))
          39 (when (< @current-slide slides-count) (next-slide))
          32 (when (< @current-slide slides-count) (next-slide))
          (.log js/console (.-keyCode key))))
      (recur)))
  (rum/mount
   (page)
   (. js/document (getElementById "container"))))
