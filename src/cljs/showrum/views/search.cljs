(ns showrum.views.search
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [beicon.core :as rx]
            [showrum.events :as evs]))

(rum/defc button < rum/reactive
  [store]
  (let [state (rx/to-atom state)
        active (rum/react (rum/cursor-in state [:search :active]))
        mdl-v (remove nil? [:fab :mini-fab :ripple (when active :accent)])]
    [:nav.search
     (mdl/button
      {:mdl mdl-v
       :on-click #(evs/emit-to! store :search :toggle-active)}
      (mdl/icon "search"))]))

(rum/defc input-field < rum/reactive
  [store]
  (let [state (rx/to-atom store)
        term (rum/react (rum/cursor-in state [:search :term]))]
    [:div.search-input
     (mdl/textfield
      {:style {:width "50rem"}}
      (mdl/textfield-input
       {:type       "text"
        :id         "search"
        :value      term
        :auto-focus true
        :on-key-down #(when (contains? #{38 40} (.-keyCode %))
                        (.preventDefault %))
        :on-change  (fn [e]
                      (.preventDefault e)
                      (.stopPropagation e)
                      (let [term (-> e .-target .-value)]
                        (evs/emit-to! store :search/term term)))})
      (mdl/textfield-label {:for "search"} "Search in the slide titles"))]))

(rum/defc results-list < rum/reactive
  [store]
  (let [state (rx/to-atom store)
        term (rum/react (rum/cursor-in state [:search :term]))]
    (if (and term (not (empty? term)))
      [:div.search-results
       (let [results (rum/react (rum/cursor-in state [:search :results]))]
         (if (seq results)
           (mdl/list
            (let [current-result (rum/react (rum/cursor-in state [:search :result]))]
              (for [[id [deck-id deck-title slide-id slide-title]]
                    (map-indexed (fn [i it] [i it]) results)]
                (mdl/li
                 {:key            (str id deck-id slide-id)
                  :icon           "present_to_all"
                  :class          (if (= id current-result) "active" "")
                  :content        (str deck-title " - " slide-title)
                  :on-mouse-enter #(evs/emit-to! store :search/active-result id)
                  :on-mouse-leave #(evs/emit-to! store :search/active-result nil)
                  :on-click       (fn [e]
                                    (evs/emit-to! store :current :deck-id deck-id)
                                    (evs/emit-to! store :current :slide slide-id)
                                    (evs/emit-to! store :search :deactivate-and-clear))}))))
           [:p "No results for \"" [:strong term] "\""]))])))

(rum/defc main < rum/reactive
  [store]
  (let [state (rx/to-atom store)
        active (rum/react (rum/cursor-in state [:search :active]))]
    (if active
      [:div.search-panel
       (input-field store)
       (results-list store)])))
