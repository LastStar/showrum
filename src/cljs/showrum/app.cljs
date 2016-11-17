(ns showrum.app
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [showrum.db :as db]
            [showrum.events :as events]))

(def current-slide (atom 1))

(rum/defc slides < rum/reactive []
  (let [[id order type title] (db/slide-by-order
                               (rum/react current-slide))]
    (case type
      :type/header
      [:div.slide.header
       {:key id}
       [:h1.title.f1 title]]
      :type/bullets
      [:div.slide.bullets
       {:key id}
       [:h1.title.f1 title]
       [:ul
        (for [item (db/bullets-for id)]
          [:li item])]])))

(rum/defc deck
  []
  [:div.deck
   (slides)])

(rum/defcs navigation <
  rum/reactive
  (rum/local false ::hovered)
  {:will-update (fn [state]
                 (let [hovered (::hovered state)]
                       (reset! hovered true)
                       (.setTimeout js/window #(reset! hovered false) 5000))
                 state)}
   [state slides-count]
   (let [hovered     (::hovered state)
         hover-class (or (and @hovered "hovered") "")]
     [:nav
      {:class          hover-class
       :on-mouse-enter #(reset! hovered true)
       :on-mouse-leave (fn [e]
                         (.setTimeout js/window #(reset! hovered false) 5000))}
      (let [active (and (> (rum/react current-slide) 1) :active)]
        (mdl/button
         {:mdl      [:fab :mini-fab]
          :on-click (when active #(swap! current-slide dec))
          :class    active}
         (mdl/icon "navigate_before")))
      [:span.counter (str (rum/react current-slide) "/" slides-count)]
      (let [active (and (< @current-slide slides-count) :active)]
        (mdl/button
         {:mdl      [:fab :mini-fab]
          :on-click (when active #(swap! current-slide inc))
          :class    active}
         (mdl/icon "navigate_next")))]))

  (rum/defc footer
    []
    (let [[author date] (db/deck)]
      [:footer
       [:div author]
       [:div date]]))

  (rum/defc page < rum/reactive
    []
    (let [slides-count  (count (db/slides))]
      [:div.page
       (navigation slides-count)
       (deck)
       (footer)]))

  (defn init []
    (db/init)
    (go-loop []
      (let [key (<! events/keydown-chan-events)
            slides-count (count (db/slides))]
        (case (.-keyCode key)
          37 (when (> @current-slide 1) (swap! current-slide dec))
          39 (when (< @current-slide slides-count) (swap! current-slide inc))
          32 (when (< @current-slide slides-count) (swap! current-slide inc))
          (.log js/console (.-keyCode key))))
      (recur))
    (rum/mount
     (page)
     (. js/document (getElementById "container"))))
