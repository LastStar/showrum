(ns showrum.views
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [showrum.db :as db]
            [showrum.parser :as parser]
            [showrum.events :as events]
            [showrum.dispatchers :as dispatchers]
            [showrum.views.presentation :as presentation]
            [goog.net.XhrIo :as xhrio]
            [scrum.dispatcher :refer [dispatch!]]
            [scrum.core :refer [subscription]]))

(defn db-initialized
  [decks-response]
  (-> decks-response .-target .getResponse parser/parse-decks db/init)
  (dispatch! :initialized :db))

(defn init-from-gist [gist-uri]
  (xhrio/send gist-uri db-initialized))

(rum/defc gist-form []
  [:div.gist
   (mdl/grid
    (mdl/cell {:mdl [:2]})
    (mdl/cell {:mdl [:8]}
              [:h4 "No decks loaded. Please add uri for the gist."]
              [:form
               {:on-submit (fn [e]
                             (.preventDefault e)
                             (init-from-gist (-> e .-target (aget "gist") .-value)))}
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
  (if (rum/react (subscription [:initialized :db]))
    (let [decks        (db/decks)
          deck         (db/deck (rum/react (subscription [:current :deck-id])))
          slides       (:deck/slides deck)
          hash         (-> js/document .-location .-hash)]
      (dispatch! :current :slides-count (count slides))
      (events/start-keyboard-loop
       {37 #(dispatch! :current :prev-slide)
        39 #(dispatch! :current :next-slide)
        32 #(dispatch! :current :next-slide)
        83 #(dispatch! :search :toggle-active)}
       {40 #(dispatch! :search :next-result)
        38 #(dispatch! :search :prev-result)
        13 #(dispatch! :search :activate-result)
        27 #(dispatch! :search :toggle-active)})
      [:div
       (if (= hash "#notes")
         (presentation/notes slides)
         (presentation/main decks deck slides))
       (footer deck)])
    (gist-form)))
