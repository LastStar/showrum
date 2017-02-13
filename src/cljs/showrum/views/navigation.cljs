(ns showrum.views.navigation
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [scrum.core :as scrum]
            [showrum.views.search :as search]))

(rum/defc slides-counter < rum/reactive
  [r slides-count]
  [:div.counter (str (rum/react (scrum/subscription r [:current :slide])) " / " slides-count)])

(rum/defc deck-chooser < rum/reactive
  [r decks]
  [:nav.decks
   {:width (str (count decks) "2vw")}
   (for [{:keys [:db/id :deck/title]} decks]
     [:div
      {:key id}
      (mdl/button
       {:mdl      [:ripple]
        :disabled (= (rum/react (scrum/subscription r [:current :deck-id])) id)
        :on-click #(scrum/dispatch! r :current :deck-id id)}
       title)])])

(rum/defc slide-navigation < rum/reactive
  [r slide slides-count]
  (let [current-slide (rum/react (scrum/subscription r [:current :slide]))]
    [:nav.slides
     (let [active (and (> current-slide 1) :active)]
       (mdl/button
        {:mdl      [:fab :mini-fab :ripple]
         :on-click (when active #(scrum/dispatch! r :current :prev-slide))
         :disabled (not active)}
        (mdl/icon "navigate_before")))
     (let [active (and (< current-slide slides-count) :active)]
       (mdl/button
        {:mdl      [:fab :mini-fab :ripple]
         :on-click (when active #(scrum/dispatch! r :current :next-slide))
         :disabled (not active)}
        (mdl/icon "navigate_next")))]))

(rum/defc reload-decks
  [r]
  [:nav.reload
   (mdl/button
    {:mdl      [:fab :mini-fab :ripple]
     :on-click (fn [e] (scrum/dispatch! r :initialized :clear-db))}
    (mdl/icon "refresh"))])

(rum/defcs main < rum/reactive
  (rum/local false ::hovered)
  (rum/local nil ::timer)
  [state r slide decks]
  (let [hovered       (::hovered state)
        timer         (::timer state)
        slides-count  (rum/react (scrum/subscription r [:current :slides-count]))
        clear-timer   #(when @timer (.clearTimeout js/window @timer))
        current-slide (rum/react (scrum/subscription r [:current :slide]))
        hover-class   (if (or @hovered
                              (= current-slide 1)
                              (= current-slide slides-count)
                              (rum/react (scrum/subscription r [:search :active])))
                        "hovered" "")]
    [:div.navigation
     {:class          hover-class
      :on-mouse-enter (fn [e] (clear-timer) (reset! hovered true))
      :on-mouse-leave (fn [e]
                        (clear-timer)
                        (reset! timer (.setTimeout js/window #(reset! hovered false) 2000)))}
     (reload-decks r)
     (search/button r)
     (deck-chooser r decks)
     (slides-counter r slides-count)
     (slide-navigation r slide slides-count)]))
