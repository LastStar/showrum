(ns showrum.views.navigation
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [scrum.dispatcher :refer [dispatch!]]
            [scrum.core :refer [subscription]]
            [showrum.db :as db]))

(rum/defc slides-counter < rum/reactive
  [slides-count]
  [:div.counter (str (rum/react (subscription [:current :slide])) " / " slides-count)])

(rum/defc deck-navigation < rum/reactive
  [decks]
  [:nav.decks
   {:width (str (count decks) "2vw")}
   (for [[id _ title] decks]
     [:div
      {:key id}
      (mdl/button
       {:mdl      [:ripple]
        :disabled (= (rum/react (subscription [:current :deck-id])) id)
        :on-click #(dispatch! :current :deck-id id)}
       title)])])

(rum/defc slide-navigation < rum/reactive
  [slide slides-count]
  (let [current-slide (rum/react (subscription [:current :slide]))]
    [:nav.slides
     (let [active (and (> current-slide 1) :active)]
       (mdl/button
        {:mdl      [:fab :mini-fab :ripple]
         :on-click (when active #(dispatch! :current :prev-slide))
         :disabled (not active)}
        (mdl/icon "navigate_before")))
     (let [active (and (< current-slide slides-count) :active)]
       (mdl/button
        {:mdl      [:fab :mini-fab :ripple]
         :on-click (when active #(dispatch! :current :next-slide))
         :disabled (not active)}
        (mdl/icon "navigate_next")))]))

(rum/defcs reload-decks
  []
  [:nav.reload
   (mdl/button
    {:mdl      [:fab :mini-fab :ripple]
     :on-click (fn [e] (dispatch! :initialized :clear-db))}
    (mdl/icon "refresh"))])

(rum/defcs search-decks < rum/reactive
  []
  (let [mdl-v (remove nil? [:fab :mini-fab :ripple
                            (when (rum/react (subscription [:search :active])) :accent)])]
    [:nav.search
     (mdl/button
      {:mdl mdl-v
       :on-click #(dispatch! :search :toggle-active)}
      (mdl/icon "search"))]))

(rum/defc search-input-field < rum/reactive
  []
  [:div.search-input
   (mdl/textfield
    {:style {:width "50rem"}}
    (mdl/textfield-input
     {:type       "text"
      :id         "search"
      :value      (rum/react (subscription [:search :term]))
      :auto-focus true
      :on-key-down #(when (contains? #{38 40} (.-keyCode %))
                      (.preventDefault %))
      :on-change  (fn [e]
                    (.preventDefault e)
                    (.stopPropagation e)
                    (let [term (-> e .-target .-value)]
                      (dispatch! :search :term term)
                      (dispatch! :search :results (db/search term))))})
    (mdl/textfield-label {:for "search"} "Search in the slide titles"))])

(rum/defc search-results-list < rum/reactive
  []
  (let [term (rum/react (subscription [:search :term]))]
    (if (and term (not (empty? term)))
      [:div.search-results
       (let [search-results (rum/react (subscription [:search :results]))]
         (if (seq search-results)
           (mdl/list
            (let [current-result (rum/react (subscription [:current :search-result]))]
              (for [[id [deck-id deck-title slide-id slide-title]]
                    (map-indexed (fn [i it] [i it]) search-results)]
                (mdl/li
                 {:key            (str id deck-id slide-id)
                  :icon           "present_to_all"
                  :class          (if (= id current-result) "active" "")
                  :content        (str deck-title " - " slide-title)
                  :on-mouse-enter #(dispatch! :search :active-result id)
                  :on-mouse-leave #(dispatch! :search :active-result nil)
                  :on-click       #(dispatch! :search :activate-result)}))))
           [:p
            "No results for \""
            [:strong (rum/react (subscription [:search :term]))]
            "\""]))])))

(rum/defcs search-panel < rum/reactive
  []
  (if (rum/react (subscription [:search :active]))
    [:div.search-panel
     (search-input-field)
     (search-results-list)]))

(rum/defcs main <
  rum/reactive
  (rum/local false ::hovered)
  (rum/local nil ::timer)
  [state slides decks]
  (let [hovered       (::hovered state)
        timer         (::timer state)
        slides-count  (rum/react (subscription [:current :slides-count]))
        clear-timer   #(when @timer (.clearTimeout js/window @timer))
        current-slide (rum/react (subscription [:current :slide]))
        hover-class   (if (or @hovered (= current-slide 1) (= current-slide slides-count) (rum/react (subscription [:search :active])))
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
