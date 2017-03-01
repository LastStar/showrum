(ns showrum.views.navigation
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [beicon.core :as rx]
            [potok.core :as ptk]
            [showrum.events :refer [NavigateNextSlide NavigatePreviousSlide
                                    SetCurrentDeck]]))

(rum/defc slides-counter
  [current-slide slides-count]
  [:div.counter (str current-slide " / " slides-count)])

(rum/defc slide-navigation < rum/reactive
  [store slides-count current-slide]
  (let [button (fn [active event icon]
                 (mdl/button
                  {:mdl      [:fab :mini-fab :ripple]
                   :on-click (when active #(ptk/emit! store event))
                   :disabled (not active)}
                  (mdl/icon icon)))]
    [:nav.slides
     (button (and (> current-slide 1) :active)
             (NavigatePreviousSlide.) "navigate_before")
     (button (and (< current-slide slides-count) :active)
             (NavigateNextSlide.) "navigate_next")]))

(rum/defc deck-chooser
  [store decks current-deck]
  [:nav.decks
   {:width (str (count decks) "2vw")}
   (for [{:keys [:deck/order :deck/title :deck/slides]} decks]
     [:div
      {:key (str current-deck order)}
      (mdl/button
       {:mdl      [:ripple]
        :disabled (= current-deck order)
        :on-click #(ptk/emit! store (SetCurrentDeck. order))}
       title)])])

(rum/defcs main < rum/reactive
  (rum/local false ::hovered)
  (rum/local nil ::timer)
  [local-state store slides decks search-button current-slide]
  (let [state         (rx/to-atom store)
        hovered       (::hovered local-state)
        timer         (::timer local-state)
        slides-count  (rum/react (rum/cursor state :slides-count))
        current-deck  (rum/react (rum/cursor state :deck))
        timeout       2000
        clear-timer   #(when @timer (.clearTimeout js/window @timer))
        set-timer     (fn []
                        (reset! timer
                                (.setTimeout js/window #(reset! hovered false)
                                             timeout)))
        search-active (rum/react (rum/cursor state :search/active))
        hover-class   (if (or @hovered
                              (= current-slide 1)
                              (= current-slide slides-count)
                              search-active)
                        "hovered" "")]
    [:div.navigation
     {:class          hover-class
      :on-mouse-enter (fn [e] (clear-timer) (reset! hovered true))
      :on-mouse-leave (fn [e] (clear-timer) (set-timer))}
     (search-button)
     (deck-chooser store decks current-deck)
     (slides-counter current-slide slides-count)
     (slide-navigation store slides-count current-slide)]))
