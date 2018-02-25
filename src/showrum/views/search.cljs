(ns showrum.views.search
  (:require [rum.core :as rum]
            [beicon.core :as rxt]
            [potok.core :as ptk]
            [showrum.events :as events]
            [showrum.material :as material]))

(rum/defc button < rum/reactive
  [store]
  (let [state (rxt/to-atom store)
        active (rum/react (rum/cursor state :search/active))]
    (material/Fab
     {:on-click #(ptk/emit! store (events/->ToggleSearchPanel))}
     "search")))

(rum/defc input-field < rum/reactive
  [store]
  (let [state (rxt/to-atom store)
        term (rum/react (rum/cursor state :search/term))]
    [:div.search-input
     (material/TextField
       {:style {:width "50rem"}
        :value      term
        :auto-focus true
        :on-key-down #(when (#{38 40} (.-keyCode %))
                        (.preventDefault %))
        :on-change  (fn [e]
                      (.preventDefault e)
                      (.stopPropagation e)
                      (let [term (-> e .-target .-value)]
                        (ptk/emit! store  (events/->SetSearchTerm term))))}
      "Search in the slide titles")]))


(rum/defc results-list < rum/reactive
  [store]
  (let [state (rxt/to-atom store)
        term (rum/react (rum/cursor state :search/term))]
    (if (and term (not (empty? term)))
      [:div.search-results
       (let [results (rum/react (rum/cursor state :search/results))]
         (if (seq results)
           [:ul
            (let [current-result (rum/react (rum/cursor state :search/result))]
              (for [[id [deck-id deck-title slide slide-title]]
                    (map-indexed (fn [i it] [i it]) results)]
                [:li
                 {:key            (str id deck-id slide)
                  :class          (if (= id current-result) "active" "")
                  :on-mouse-enter #(ptk/emit! store (events/->SetActiveSearchResult id))
                  :on-mouse-leave #(ptk/emit! store (events/->SetActiveSearchResult nil))
                  :on-click       #(ptk/emit! store (events/->ActivateSearchResult id))}
                 (str deck-title " - " slide-title)]))]
           [:p "No results for \"" [:strong term] "\""]))])))

(rum/defc main < rum/reactive
  [store]
  (let [state (rxt/to-atom store)
        active (rum/react (rum/cursor state :search/active))]
    (if active
      [:div.search-panel
       (input-field store)
       (results-list store)])))
