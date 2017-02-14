(ns showrum.views.search
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [scrum.core :as scrum]
            [showrum.db :as db]))

(rum/defc button < rum/reactive
  [r]
  (let [active (rum/react (scrum/subscription r [:search :active]))
        mdl-v (remove nil? [:fab :mini-fab :ripple (when active :accent)])]
    [:nav.search
     (mdl/button
      {:mdl mdl-v
       :on-click #(scrum/dispatch! r :search :toggle-active)}
      (mdl/icon "search"))]))

(rum/defc input-field < rum/reactive
  [r]
  (let [term (rum/react (scrum/subscription r [:search :term]))]
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
                        (scrum/dispatch! r :search :term term)
                        (scrum/dispatch! r :search :results (db/search term))))})
      (mdl/textfield-label {:for "search"} "Search in the slide titles"))]))

(rum/defc results-list < rum/reactive
  [r]
  (let [term (rum/react (scrum/subscription r [:search :term]))]
    (if (and term (not (empty? term)))
      [:div.search-results
       (let [results (rum/react (scrum/subscription r [:search :results]))]
         (if (seq results)
           (mdl/list
            (let [current-result (rum/react (scrum/subscription r [:search :result]))]
              (for [[id [deck-id deck-title slide-id slide-title]]
                    (map-indexed (fn [i it] [i it]) results)]
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
           [:p "No results for \"" [:strong term] "\""]))])))

(rum/defc main < rum/reactive
  [r]
  (let [active (rum/react (scrum/subscription r [:search :active]))]
    (if active
    [:div.search-panel
     (input-field r)
     (results-list r)])))
