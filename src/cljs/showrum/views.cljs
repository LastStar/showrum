(ns showrum.views
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [beicon.core :as rxt]
            [potok.core :as ptk]
            [showrum.events :refer [->InitializeGist]]
            [showrum.views.navigation :as navigation]
            [showrum.views.search :as search]
            [showrum.views.presentation :as presentation]))

(rum/defc gist-form [store err]
  [:div.gist
   (mdl/grid
    (mdl/cell {:mdl [:2]})
    (mdl/cell {:mdl [:8]}
              [:h4 "No decks loaded. Please add uri for the gist."]
              [:h5
               {:style {:color :red}}
               (when err (str "There was a " err))]
              [:form
               {:on-submit (fn [e]
                             (.preventDefault e)
                             (ptk/emit! store
                                        (->InitializeGist
                                         (-> e .-target (aget "gist") .-value))))}
               [:div
                (mdl/textfield
                 {:style {:width "50rem"}}
                 (mdl/textfield-input {:type "text" :id "gist"})
                 (mdl/textfield-label {:for "gist"} "Gist URI"))]
               [:div (mdl/button {:mdl [:raised :ripple]} "Parse")]])
    (mdl/cell {:mdl [:2]}))])

(rum/defc footer [deck gist]
  (let [{:deck/keys [author date place]} deck]
    [:footer
     [:div gist]
     [:div author]
     [:div date]
     (when [:div place])]))

(rum/defc loading []
  [:div.loading
   [:h2 "Initializing DB"]
   (mdl/spinner {:is-active true})])

(rum/defc main < rum/reactive [store]
  (let [state (rxt/to-atom store)
        db    (rum/react (rum/cursor state :db/decks))
        gist  (rum/react (rum/cursor state :db/gist))
        err   (rum/react (rum/cursor state :db/error))]
    [:div
     (if gist
       (if db
         (let [current-deck  (rum/react (rum/cursor state :deck/current))
               current-slide (rum/react (rum/cursor state :slide/current))
               deck          (some #(and (= current-deck (:deck/order %)) %) db)
               slides        (:deck/slides deck)]
           [:div
            [:div.page
             (navigation/main store slides db #(search/button store) current-slide)
             (search/main store)
             (presentation/main slides current-slide)
             (footer deck gist)]])
         (loading))
       (gist-form store err))]))
