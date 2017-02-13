(ns showrum.views
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [scrum.core :as scrum]
            [showrum.events :as events]
            [showrum.effects :refer [init-from-gist]]
            [showrum.views.presentation :as presentation]))

(rum/defc gist-form [r]
  [:div.gist
   (mdl/grid
    (mdl/cell {:mdl [:2]})
    (mdl/cell {:mdl [:8]}
              [:h4 "No decks loaded. Please add uri for the gist."]
              [:form
               {:on-submit (fn [e]
                             (.preventDefault e)
                             (init-from-gist r (-> e .-target (aget "gist") .-value)))}
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

(rum/defc main < rum/reactive [r]
  (if (rum/react (scrum/subscription r [:initialized :db]))
    (let [decks        @(scrum/subscription r [:initialized :decks])
          deck-id      (rum/react (scrum/subscription r [:current :deck-id]))
          deck         (some #(and (= deck-id (:db/id %)) %) decks)
          slides       (:deck/slides deck)
          hash         (-> js/document .-location .-hash)]
      (scrum/dispatch! r :current :slides-count (count slides))
      (events/start-keyboard-loop
       {37 #(scrum/dispatch! r :current :prev-slide)
        39 #(scrum/dispatch! r :current :next-slide)
        32 #(scrum/dispatch! r :current :next-slide)
        83 #(scrum/dispatch! r :search :toggle-active)}
       {40 #(scrum/dispatch! r :search :next-result)
        38 #(scrum/dispatch! r :search :prev-result)
        13 #(let [[deck-id _ slide _]
                  (get @(scrum/subscription r [:search :results])
                       @(scrum/subscription r [:search :result]))]
              (scrum/dispatch! r :current :deck-id deck-id)
              (scrum/dispatch! r :current :slide slide)
              (scrum/dispatch! r :search :toggle-active)
              (scrum/dispatch! r :search :term ""))
        27 #(scrum/dispatch! r :search :toggle-active)})
      [:div
       (presentation/main r decks deck slides)
       (footer deck)])
    (gist-form r)))
