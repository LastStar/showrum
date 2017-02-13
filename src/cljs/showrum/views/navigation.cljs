(ns showrum.views.navigation
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [scrum.core :as scrum]
            [showrum.db :as db]))

(rum/defc slides-counter < rum/reactive
  [r slides-count]
  [:div.counter (str (rum/react (scrum/subscription r [:current :slide])) " / " slides-count)])

(rum/defc deck-navigation < rum/reactive
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

(rum/defc search-decks < rum/reactive
  [r]
  (let [mdl-v (remove nil? [:fab :mini-fab :ripple
                            (when (rum/react (scrum/subscription r [:search :active])) :accent)])]
    [:nav.search
     (mdl/button
      {:mdl mdl-v
       :on-click #(scrum/dispatch! r :search :toggle-active)}
      (mdl/icon "search"))]))

(rum/defc search-input-field < rum/reactive
  [r]
  [:div.search-input
   (mdl/textfield
    {:style {:width "50rem"}}
    (mdl/textfield-input
     {:type       "text"
      :id         "search"
      :value      (rum/react (scrum/subscription r [:search :term]))
      :auto-focus true
      :on-key-down #(when (contains? #{38 40} (.-keyCode %))
                      (.preventDefault %))
      :on-change  (fn [e]
                    (.preventDefault e)
                    (.stopPropagation e)
                    (let [term (-> e .-target .-value)]
                      (scrum/dispatch! r :search :term term)
                      (scrum/dispatch! r :search :results (db/search term))))})
    (mdl/textfield-label {:for "search"} "Search in the slide titles"))])

(rum/defc search-results-list < rum/reactive
  [r]
  (let [term (rum/react (scrum/subscription r [:search :term]))]
    (if (and term (not (empty? term)))
      [:div.search-results
       (let [search-results (rum/react (scrum/subscription r [:search :results]))]
         (if (seq search-results)
           (mdl/list
            (let [current-result (rum/react (scrum/subscription r [:search :result]))]
              (for [[id [deck-id deck-title slide-id slide-title]]
                    (map-indexed (fn [i it] [i it]) search-results)]
                (mdl/li
                 {:key            (str id deck-id slide-id)
                  :icon           "present_to_all"
                  :class          (if (= id current-result) "active" "")
                  :content        (str deck-title " - " slide-title)
                  :on-mouse-enter #(scrum/dispatch! r :search :active-result id)
                  :on-mouse-leave #(scrum/dispatch! r :search :active-result nil)
                  :on-click       (fn [e]
                                    (scrum/dispatch! r :current :deck-id deck-id)
                                    (scrum/dispatch! r :current :slide slide-id)
                                    (scrum/dispatch! r :search :deactivate-and-clear))}))))
           [:p
            "No results for \""
            [:strong (rum/react (scrum/subscription r [:search :term]))]
            "\""]))])))

(rum/defc search-panel < rum/reactive
  [r]
  (if (rum/react (scrum/subscription r [:search :active]))
    [:div.search-panel
     (search-input-field r)
     (search-results-list r)]))

(rum/defcs main < rum/reactive
  (rum/local false ::hovered)
  (rum/local nil ::timer)
  [state r slides decks]
  (let [hovered       (::hovered state)
        timer         (::timer state)
        slides-count  (rum/react (scrum/subscription r [:current :slides-count]))
        clear-timer   #(when @timer (.clearTimeout js/window @timer))
        current-slide (rum/react (scrum/subscription r [:current :slide]))
        hover-class   (if (or @hovered (= current-slide 1) (= current-slide slides-count) (rum/react (scrum/subscription r [:search :active])))
                        "hovered" "")]
    [:div.navigation
     {:class          hover-class
      :on-mouse-enter (fn [e] (clear-timer) (reset! hovered true))
      :on-mouse-leave (fn [e]
                        (clear-timer)
                        (reset! timer (.setTimeout js/window #(reset! hovered false) 2000)))}
     (reload-decks r)
     (search-decks r)
     (deck-navigation r decks)
     (slides-counter r slides-count)
     (slide-navigation r slides slides-count)]))
