(ns showrum.views
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [beicon.core :as rxt]
            [potok.core :as ptk]
            [showrum.events :refer [InitializeGist SetCurentSlidesCount]]
            [showrum.views.navigation :as navigation]
            [showrum.views.search :as search]
            [showrum.views.presentation :as presentation]))

(rum/defc gist-form [store]
  [:div.gist
   (mdl/grid
    (mdl/cell {:mdl [:2]})
    (mdl/cell {:mdl [:8]}
              [:h4 "No decks loaded. Please add uri for the gist."]
              [:form
               {:on-submit (fn [e]
                             (.preventDefault e)
                             (js/console.log "click")
                             (ptk/emit! store
                                        (InitializeGist.
                                         (-> e .-target (aget "gist") .-value))))}
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


(rum/defc main < rum/reactive [store]
  (let [state            (rxt/to-atom store)
        db               (rum/react (rum/cursor-in state [:db]))
        gist-initialized (rum/react (rum/cursor-in state [:gist]))]
    [:div
     (if gist-initialized
       (if db
         (let [current-deck (:deck @state)
               deck         (some #(and (= current-deck (:deck/order %)) %) db)
               slides (:deck/slides deck)]
           [:div
            [:div.page
             (navigation/main store slides db #(search/button store))
             (presentation/main state slides)]])
         (loading))
       (gist-form store))]))
