(ns showrum.views.search
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [scrum.core :as scrum]
            [showrum.effects :as effects]))

(rum/defc button < rum/reactive
  [reconciler]
  (let [active (rum/react (scrum/subscription reconciler [:search :active]))
        mdl-v (remove nil? [:fab :mini-fab :ripple (when active :accent)])]
    [:nav.search
     (mdl/button
      {:mdl mdl-v
       :on-click #(scrum/dispatch! reconciler :search :toggle-active)}
      (mdl/icon "search"))]))

(rum/defc input-field < rum/reactive
  [reconciler]
  (let [term (rum/react (scrum/subscription reconciler [:search :term]))]
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
                        (scrum/dispatch! reconciler :search :term term)
                        (effects/search reconciler term)))})
      (mdl/textfield-label {:for "search"} "Search in the slide titles"))]))

(rum/defc results-list < rum/reactive
  [reconciler]
  (let [term (rum/react (scrum/subscription reconciler [:search :term]))]
    (if (and term (not (empty? term)))
      [:div.search-results
       (let [results (rum/react (scrum/subscription reconciler [:search :results]))]
         (if (seq results)
           (mdl/list
            (let [current-result (rum/react (scrum/subscription reconciler [:search :result]))]
              (for [[id [deck-id deck-title slide-id slide-title]]
                    (map-indexed (fn [i it] [i it]) results)]
                (mdl/li
                 {:key            (str id deck-id slide-id)
                  :icon           "present_to_all"
                  :class          (if (= id current-result) "active" "")
                  :content        (str deck-title " - " slide-title)
                  :on-mouse-enter #(scrum/dispatch! reconciler :search :active-result id)
                  :on-mouse-leave #(scrum/dispatch! reconciler :search :active-result nil)
                  :on-click       (fn [e]
                                    (scrum/dispatch! reconciler :current :deck-id deck-id)
                                    (scrum/dispatch! reconciler :current :slide slide-id)
                                    (scrum/dispatch! reconciler :search :deactivate-and-clear))}))))
           [:p "No results for \"" [:strong term] "\""]))])))

(rum/defc main < rum/reactive
  [reconciler]
  (let [active (rum/react (scrum/subscription reconciler [:search :active]))]
    (if active
      [:div.search-panel
       (input-field reconciler)
       (results-list reconciler)])))
