(ns showrum.views
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [pushy.core :as pushy]
            [scrum.core :as scrum]
            [scrum.router :as router]
            [showrum.events :as events]
            [showrum.effects :refer [init-from-gist]]
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

(rum/defc main < rum/reactive [reconciler history]
  (let [[route params query] (rum/react (scrum/subscription reconciler [:router :route]))]
    (case route
      :index
      (do
        (if-let [url (:url query)]
          (init-from-gist reconciler history url))
        (gist-form reconciler history))
      :presentation
      (if (rum/react (scrum/subscription reconciler [:initialized :db]))
        (let [decks        @(scrum/subscription reconciler [:initialized :decks])
              deck-id      (rum/react (scrum/subscription reconciler [:current :deck-id]))
              deck         (some #(and (= deck-id (:db/id %)) %) decks)
              slides       (:deck/slides deck)
              hash         (-> js/document .-location .-hash)]
          (scrum/dispatch! reconciler :current :slides-count (count slides))
          (events/start-keyboard-loop reconciler
           {37 #(scrum/dispatch! reconciler :current :prev-slide)
            39 #(scrum/dispatch! reconciler :current :next-slide)
            32 #(scrum/dispatch! reconciler :current :next-slide)
            83 #(scrum/dispatch! reconciler :search :toggle-active)}
           {40 #(scrum/dispatch! reconciler :search :next-result)
            38 #(scrum/dispatch! reconciler :search :prev-result)
            13 #(let [[deck-id _ slide _]
                      (get @(scrum/subscription reconciler [:search :results])
                           @(scrum/subscription reconciler [:search :result]))]
                  (scrum/dispatch! reconciler :current :deck-id deck-id)
                  (scrum/dispatch! reconciler :current :slide slide)
                  (scrum/dispatch! reconciler :search :toggle-active)
                  (scrum/dispatch! reconciler :search :term ""))
            27 #(scrum/dispatch! reconciler :search :toggle-active)})
          [:div
           [:div.page
            (navigation/main reconciler history slides decks #(search/button reconciler))
            (search/main reconciler)
            (presentation/main reconciler slides)]
           (footer deck)])
        [:div.loading
         [:h2 "Initializing DB"]]))))
