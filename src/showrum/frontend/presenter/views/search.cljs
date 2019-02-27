(ns showrum.frontend.presenter.views.search
  (:require [hx.react :as hx]
            [beicon.core :as rxt]
            [potok.core :as ptk]

            [showrum.frontend.hooks :as hooks]
            [showrum.frontend.material :as material]
            [showrum.frontend.presenter.events :as events]))

(hx/defnc InputField [{store :store}]
  (let [state (rxt/to-atom store)
        term  (hooks/<-derive state :search/term)]
    [:div {:class "search-input"}
     [material/TextField
      {:style       {:width "50rem"}
       :value       term
       :auto-focus  true
       :on-key-down #(when (#{38 40} (.-keyCode %))
                       (.preventDefault %))
       :on-change   (fn [e]
                      (.preventDefault e)
                      (.stopPropagation e)
                      (let [term (-> e .-target .-value)]
                        (ptk/emit! store  (events/->SetSearchTerm term))))
       :name        "search"}
      [material/FloatingLabel {:input-name "search" :label "Search"}]]]))

(hx/defnc ResultsList [{store :store}]
  (let [state (rxt/to-atom store)
        derive-fn (fn [s] (select-keys s [:search/term :search/results :search/result]))
        {term :search/term
         results :search/results
         current-result :search/result} (hooks/<-derive state derive-fn)]
    (if (and term (not (empty? term)))
      [:div {:class "search-results"}
         (if (seq results)
           [:ul {:class "mdc-list"} 
              (for [[id [deck-id deck-title slide slide-title]]
                    (map-indexed (fn [i it] [i it]) results)]
                [:li
                 {:key            (str id deck-id slide)
                  :class          (str "mdc-list-item "
                                       (when (= id current-result) "active"))
                  :on-mouse-enter #(ptk/emit! store (events/->SetActiveSearchResult id))
                  :on-mouse-leave #(ptk/emit! store (events/->SetActiveSearchResult nil))
                  :on-click       #(ptk/emit! store (events/->ActivateSearchResult id))}
                 (str deck-title " - " slide-title)])]
           [:p "No results for \"" [:strong term] "\""])])))

(hx/defnc Main [{store :store}]
  (let [state  (rxt/to-atom store)
        active (hooks/<-derive state :search/active)]
    (js/console.log @state active)
    (when active
      [:div {:class "search-panel"}
       [InputField {:store store}]
       [ResultsList {:store store}]])))
