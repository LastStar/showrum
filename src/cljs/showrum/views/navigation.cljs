(ns showrum.views.navigation
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [showrum.state :as state]
            [showrum.db :as db]))

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
        :disabled (= (rum/react state/current-deck-id) id)
        :on-click (fn [e] (state/set-deck-id id))}
       title)])])

(rum/defc slide-navigation < rum/reactive
  [slide slides-count]
  [:nav.slides
   (let [active (and (> (rum/react state/current-slide) 1) :active)]
     (mdl/button
      {:mdl      [:fab :mini-fab :ripple]
       :on-click (when active #(state/prev-slide))
       :disabled (not active)}
      (mdl/icon "navigate_before")))
   (let [active (and (< (rum/react state/current-slide) slides-count) :active)]
     (mdl/button
      {:mdl      [:fab :mini-fab :ripple]
       :on-click (when active #(state/next-slide))
       :disabled (not active)}
      (mdl/icon "navigate_next")))])

(rum/defcs reload-decks
  []
  [:nav.reload
   (mdl/button
    {:mdl      [:fab :mini-fab :ripple]
     :on-click (fn [e] (state/db-cleared))}
    (mdl/icon "refresh"))])

(rum/defcs search-decks < rum/reactive
  []
  (let [mdl-v (remove nil? [:fab :mini-fab :ripple
                            (when (rum/react state/searching) :accent)])]
    [:nav.search
     (mdl/button
      {:mdl mdl-v
       :on-click state/search-toggler}
      (mdl/icon "search"))]))

(rum/defcs search-panel < rum/reactive
  []
  (if (rum/react state/searching)
    [:div.search-panel
     [:div.search-input
      (mdl/textfield
       {:style {:width "50rem"}}
       (mdl/textfield-input
        {:type      "text"
         :id        "search"
         :value (rum/react state/search-term)
         :auto-focus true
         :on-change state/search-term-updater})
       (mdl/textfield-label {:for "search"} "Search in the slide titles"))]
     (let [term (rum/react state/search-term)]
       (if (and term (not (empty? term)))
         [:div.search-results
          (let [search-results @state/search-results]
            (if (seq search-results)
              (mdl/list
               (for [res search-results]
                 (mdl/li
                  {:key (str (get res 0) (get res 2))
                   :content (str (get res 1) " - " (get res 3))
                   :on-click #(state/activate-search-result res)})))
            [:p
             "No results for \""
             [:strong (rum/react state/search-term)]
             "\""]))]))]))

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
        hover-class   (if (or @hovered (= current-slide 1) (= current-slide slides-count) (rum/react state/searching))
                        "hovered" "")]
    [:div.navigation
     {:class          hover-class
      :on-mouse-enter (fn [e] (clear-timer) (reset! hovered true))
      :on-mouse-leave (fn [e]
                        (clear-timer)
                        (reset! timer (.setTimeout js/window #(reset! hovered false) 2000)))}
     (reload-decks)
     (search-decks)
     (deck-navigation decks)
     (slides-counter slides-count)
     (slide-navigation slides slides-count)]))
