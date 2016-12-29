(ns showrum.views.navigation
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [showrum.state :as state]))

(rum/defc slides-counter < rum/reactive
  [slides-count]
  [:div.counter (str (rum/react state/current-slide) " / " slides-count)])

(rum/defc deck-navigation < rum/reactive
  [decks]
  [:nav.decks
   {:width (str (count decks) "2vw")}
   (for [[id _ title] decks]
     [:div
      {:key id}
      (mdl/button
       {:mdl      [:ripple]
        :disabled (= (rum/react state/current-deck) id)
        :on-click (fn [e] (state/set-deck id))}
       title)])])

(rum/defc slide-navigation < rum/reactive
  [slide slides-count]
  [:nav.slides
   (let [active (and (> (rum/react state/current-slide) 1) :active)]
     (mdl/button
      {:mdl      [:fab :mini-fab]
       :on-click (when active #(state/prev-slide))
       :disabled (not active)}
      (mdl/icon "navigate_before")))
   (let [active (and (< (rum/react state/current-slide) slides-count) :active)]
     (mdl/button
      {:mdl      [:fab :mini-fab]
       :on-click (when active #(state/next-slide))
       :disabled (not active)}
      (mdl/icon "navigate_next")))])

(rum/defcs reload-decks
  []
  [:nav.reload
   (mdl/button
    {:mdl      [:fab :mini-fab]
     :on-click (fn [e] (state/db-cleared))}
    (mdl/icon "refresh"))])

(rum/defcs main <
  rum/reactive
  (rum/local false ::hovered)
  (rum/local nil ::timer)
  [state slides decks]
  (let [hovered       (::hovered state)
        timer         (::timer state)
        slides-count  (count slides)
        clear-timer   #(when @timer (.clearTimeout js/window @timer))
        current-slide (rum/react state/current-slide)
        hover-class   (if (or @hovered (= current-slide 1) (= current-slide slides-count))
                        "hovered" "")]
    [:div.navigation
     {:class          hover-class
      :on-mouse-enter (fn [e] (clear-timer) (reset! hovered true))
      :on-mouse-leave (fn [e]
                        (clear-timer)
                        (reset! timer (.setTimeout js/window #(reset! hovered false) 2000)))}
     (reload-decks)
     (deck-navigation decks)
     (slides-counter slides-count)
     (slide-navigation slides slides-count)]))

