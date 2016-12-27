(ns showrum.views
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [showrum.state :as state]
            [showrum.events :as events]
            [showrum.db :as db]
            [showrum.views.presentation :as presentation]))

(rum/defc gist-form []
  [:div.gist
   (mdl/grid
    (mdl/cell {:mdl [:2]})
    (mdl/cell {:mdl [:8]}
              [:h4 "No decks loaded. Please add uri for the gist."]
              [:form
               {:on-submit (fn [e]
                             (.preventDefault e)
                             (db/init-from-gist (-> e .-target (aget "gist") .-value)))}
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

(rum/defc main < rum/reactive []
  (if (rum/react state/db-initialized?)
    (let [decks        (db/decks)
          deck         (db/deck (rum/react state/current-deck))
          slides       (:deck/slides deck)
          slides-count (count slides)
          hash         (-> js/document .-location .-hash)]
      (when-not @state/loop-running?
        (events/start-keyboard-loop
         {37 #(when (> @state/current-slide 1) (state/prev-slide))
          39 #(when (< @state/current-slide slides-count) (state/next-slide))
          32 #(when (< @state/current-slide slides-count) (state/next-slide))})
        (state/loop-running))
      [:div
       (if (= hash "#notes")
         (presentation/notes slides)
         (presentation/main decks deck slides))
       (footer deck)])
    (gist-form)))
