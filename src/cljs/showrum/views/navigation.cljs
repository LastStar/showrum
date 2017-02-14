(ns showrum.views.navigation
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [scrum.core :as scrum]
            [pushy.core :refer [set-token!]]))

(rum/defc slides-counter < rum/reactive
  [reconciler slides-count]
  [:div.counter (str (rum/react (scrum/subscription reconciler [:current :slide])) " / " slides-count)])

(rum/defc slide-navigation < rum/reactive
  [reconciler slide slides-count]
  (let [current-slide (rum/react (scrum/subscription reconciler [:current :slide]))]
    [:nav.slides
     (let [active (and (> current-slide 1) :active)]
       (mdl/button
        {:mdl      [:fab :mini-fab :ripple]
         :on-click (when active #(scrum/dispatch! reconciler :current :prev-slide))
         :disabled (not active)}
        (mdl/icon "navigate_before")))
     (let [active (and (< current-slide slides-count) :active)]
       (mdl/button
        {:mdl      [:fab :mini-fab :ripple]
         :on-click (when active #(scrum/dispatch! reconciler :current :next-slide))
         :disabled (not active)}
        (mdl/icon "navigate_next")))]))

(rum/defc deck-chooser < rum/reactive
  [reconciler decks]
  [:nav.decks
   {:width (str (count decks) "2vw")}
   (for [{:keys [:db/id :deck/title]} decks]
     [:div
      {:key id}
      (mdl/button
       {:mdl      [:ripple]
        :disabled (= (rum/react (scrum/subscription reconciler [:current :deck-id])) id)
        :on-click #(scrum/dispatch! reconciler :current :deck-id id)}
       title)])])

(rum/defc reload-decks
  [reconciler history]
  [:nav.reload
   (mdl/button
    {:mdl      [:fab :mini-fab :ripple]
     :on-click (fn [e]
                 (scrum/dispatch! reconciler :initialized :init)
                 (scrum/dispatch! reconciler :current :init)
                 (set-token! history "/")
                 (scrum/dispatch! reconciler :router :push [:index nil nil]))}
    (mdl/icon "refresh"))])

(rum/defcs main < rum/reactive
  (rum/local false ::hovered)
  (rum/local nil ::timer)
  [state reconciler history slides decks search-button]
  (let [hovered       (::hovered state)
        timer         (::timer state)
        slides-count  (rum/react (scrum/subscription reconciler [:current :slides-count]))
        timeout       2000
        clear-timer   #(when @timer (.clearTimeout js/window @timer))
        set-timer     (fn []
                        (reset! timer
                                (.setTimeout js/window
                                             #(reset! hovered false)
                                             timeout)))
        current-slide (rum/react (scrum/subscription reconciler [:current :slide]))
        active (rum/react (scrum/subscription reconciler [:search :active]))
        hover-class   (if (or @hovered
                              (= current-slide 1)
                              (= current-slide slides-count)
                              active)
                        "hovered" "")]
    [:div.navigation
     {:class          hover-class
      :on-mouse-enter (fn [e] (clear-timer) (reset! hovered true))
      :on-mouse-leave (fn [e] (clear-timer) (set-timer))}
     (reload-decks reconciler history)
     (search-button)
     (deck-chooser reconciler decks)
     (slides-counter reconciler slides-count)
     (slide-navigation reconciler slides slides-count)]))
