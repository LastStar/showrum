(ns showrum.views.navigation
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [beicon.core :as rx]
            [potok.core :as ptk]
            [showrum.events :refer [NavigateNextSlide NavigatePreviousSlide
                                    SetCurrentDeck]]))

(rum/defc slides-counter < rum/reactive
  [state slides-count]
  (let [current-slide (rum/react (rum/cursor-in state [:slide]))]
    [:div.counter (str current-slide " / " slides-count)]))

(rum/defc slide-navigation < rum/reactive
  [store slides-count]
  (let [state         (rx/to-atom store)
        current-slide (rum/react (rum/cursor-in state [:slide]))]
    [:nav.slides
     (let [active (and (> current-slide 1) :active)]
       (mdl/button
        {:mdl      [:fab :mini-fab :ripple]
         :on-click (when active #(ptk/emit! store (NavigatePreviousSlide.)))
         :disabled (not active)}
        (mdl/icon "navigate_before")))
     (let [active (and (< current-slide slides-count) :active)]
       (mdl/button
        {:mdl      [:fab :mini-fab :ripple]
         :on-click (when active #(ptk/emit! store (NavigateNextSlide.)))
         :disabled (not active)}
        (mdl/icon "navigate_next")))]))

(rum/defc deck-chooser < rum/reactive
  [store decks]
  [:nav.decks
   {:width (str (count decks) "2vw")}
   (let [state (rx/to-atom store)]
     (for [{:keys [:deck/order :deck/title]} decks]
       [:div
        {:key order}
        (mdl/button
         {:mdl      [:ripple]
          :disabled (= (rum/react (rum/cursor-in state [:deck])) order)
          :on-click #(ptk/emit! store (SetCurrentDeck. order))}
         title)]))])

(rum/defcs main < rum/reactive
  (rum/local false ::hovered)
  (rum/local nil ::timer)
  [local-state store slides decks search-button]
  (let [state         (rx/to-atom store)
        hovered       (::hovered local-state)
        timer         (::timer local-state)
        slides-count  (rum/react (rum/cursor-in state [:slides-count]))
        timeout       2000
        clear-timer   #(when @timer (.clearTimeout js/window @timer))
        set-timer     (fn []
                        (reset! timer
                                (.setTimeout js/window
                                             #(reset! hovered false)
                                             timeout)))
        current-slide (rum/react (rum/cursor-in state [:current :slide]))
        active        (rum/react (rum/cursor-in state [:search :active]))
        hover-class   (if (or @hovered
                              (= current-slide 1)
                              (= current-slide slides-count)
                              active)
                        "hovered" "")]
    [:div.navigation
     {:class          hover-class
      :on-mouse-enter (fn [e] (clear-timer) (reset! hovered true))
      :on-mouse-leave (fn [e] (clear-timer) (set-timer))}
     (search-button)
     (deck-chooser store decks)
     (slides-counter state slides-count)
     (slide-navigation store slides-count)]))
