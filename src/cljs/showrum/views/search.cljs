(ns showrum.views.search
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [beicon.core :as rxt]
            [potok.core :as ptk]
            [showrum.events :as events]))

(rum/defc button < rum/reactive
  [store]
  (let [state (rxt/to-atom store)
        active (rum/react (rum/cursor state :search/active))
        mdl-v (remove nil? [:fab :mini-fab :ripple (when active :accent)])]
    (mdl/button
     {:mdl mdl-v
      :on-click #(ptk/emit! store (events/->ToggleSearchPanel))}
     (mdl/icon "search"))))

(rum/defc input-field < rum/reactive
  [store]
  (let [state (rxt/to-atom store)
        term (rum/react (rum/cursor state :search/term))]
    [:div.search-input
     (mdl/textfield
      {:style {:width "50rem"}}
      (mdl/textfield-input
       {:type       "text"
        :id         "search"
        :value      term
        :auto-focus true
        :on-key-down #(when (#{38 40} (.-keyCode %))
                        (.preventDefault %))
        :on-change  (fn [e]
                      (.preventDefault e)
                      (.stopPropagation e)
                      (let [term (-> e .-target .-value)]
                        (ptk/emit! store  (events/->SetSearchTerm term))))})
      (mdl/textfield-label {:for "search"} "Search in the slide titles"))]))

(rum/defc results-list < rum/reactive
  [store]
  (let [state (rxt/to-atom store)
        term (rum/react (rum/cursor state :search/term))]
    (if (and term (not (empty? term)))
      [:div.search-results
       (let [results (rum/react (rum/cursor state :search/results))]
         (if (seq results)
           (mdl/list
            (let [current-result (rum/react (rum/cursor state :search/result))]
              (for [[id [deck-id deck-title slide slide-title]]
                    (map-indexed (fn [i it] [i it]) results)]
                (mdl/li
                 {:key            (str id deck-id slide)
                  :icon           "present_to_all"
                  :class          (if (= id current-result) "active" "")
                  :content        (str deck-title " - " slide-title)
                  :on-mouse-enter #(ptk/emit! store (events/->SetActiveSearchResult id))
                  :on-mouse-leave #(ptk/emit! store (events/->SetActiveSearchResult nil))
                  :on-click       #(ptk/emit! store (events/->ActivateSearchResult id))}))))
           [:p "No results for \"" [:strong term] "\""]))])))

(rum/defc main < rum/reactive
  [store]
  (let [state (rxt/to-atom store)
        active (rum/react (rum/cursor state :search/active))]
    (if active
      [:div.search-panel
       (input-field store)
       (results-list store)])))
