(ns showrum.views
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [scrum.core :as scrum]
            [showrum.events :as events]
            [showrum.effects :refer [init-from-gist go-home]]
            [showrum.views.navigation :as navigation]
            [showrum.views.search :as search]
            [showrum.views.presentation :as presentation]))

(rum/defc gist-form [reconciler history]
  [:div.gist
   (mdl/grid
    (mdl/cell {:mdl [:2]})
    (mdl/cell {:mdl [:8]}
              [:h4 "No decks loaded. Please add uri for the gist."]
              [:form
               {:on-submit (fn [e]
                             (.preventDefault e)
                             (init-from-gist reconciler history (-> e .-target (aget "gist") .-value)))}
               [:div
                (mdl/textfield
                 {:style {:width "50rem"}}
                 (mdl/textfield-input {:type "text" :id "gist"})
                 (mdl/textfield-label {:for "gist"} "Gist URI"))]
               [:div (mdl/button {:mdl [:raised :ripple]} "Parse")]])
    (mdl/cell {:mdl [:2]}))])

(rum/defc footer [deck]
  (let [{:keys [:deck/author :deck/date :deck/place]} deck]
    [:footer
     [:div author]
     [:div date]
     (when [:div place])]))

(rum/defc loading []
  [:div.loading
   [:h2 "Initializing DB"]
   (mdl/spinner {:is-active true})])

(rum/defc main < rum/reactive [reconciler history]
  (let [[route params query]
        (rum/react (scrum/subscription reconciler [:router :route]))]
    (case route
      :index
      (do
        (if-let [url (:url query)]
          (do
            (init-from-gist reconciler history url)
            (loading))
          (gist-form reconciler history)))
      :presentation
      (if (rum/react (scrum/subscription reconciler [:initialized :db]))
        (let [decks        @(scrum/subscription reconciler [:initialized :decks])
              deck-id      (rum/react (scrum/subscription reconciler [:current :deck-id]))
              deck         (some #(and (= deck-id (:db/id %)) %) decks)
              slides       (:deck/slides deck)
              hash         (-> js/document .-location .-hash)]
          (scrum/dispatch! reconciler :current :slides-count (count slides))
          (events/start-keyboard-loop reconciler)
          [:div
           [:div.page
            (navigation/main reconciler history slides decks
                             #(search/button reconciler))
            (search/main reconciler)
            (presentation/main reconciler slides)]
           (footer deck)])
        (do
          (when-not (rum/react (scrum/subscription reconciler [:initialized :gist]))
            (go-home reconciler history))
          (loading))))))
