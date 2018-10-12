(ns showrum.frontend.presenter.views
  (:require [rum.core :as rum]
            [mdc-rum.core :as mdc]
            [mdc-rum.components :as mdcc]
            [beicon.core :as rxt]
            [potok.core :as ptk]
            [showrum.frontend.presenter.events :as events]
            [showrum.frontend.presenter.views.navigation :as navigation]
            [showrum.frontend.presenter.views.search :as search]
            [showrum.frontend.presenter.views.presentation :as presentation])
  (:import goog.events.EventType))

(rum/defc gist-form [store err]
  [:div.gist
   {:style {:margin "1rem auto" :width "66vw"}}
   [mdc/typo-headline-4 "No decks loaded. Please add uri for the gist."]
   (when err
     [mdc/typo-headline-5 {:style {:color :red}} (str "There was a " err)])
   [:form
    {:on-submit (fn [e]
                  (.preventDefault e)
                  (ptk/emit! store
                             (events/->InitializeGist
                              (-> e .-target (aget "gist") .-value))))}
    (mdcc/text-field {:style {:width "66vw"}} :gist "Gist URL")
    [:div
     {:style {:text-align :right}}
     (mdcc/button {} "Parse")]]])

(rum/defc footer < rum/reactive [store deck gist current-slide]
  (let [state                            (rxt/to-atom store)
        {:deck/keys [author date place]} deck
        hovered                          (-> state (rum/cursor :navigation/hovered) rum/react)

        slides-count  (->  state (rum/cursor :deck/slides-count) rum/react)
        hover-class (if (or hovered
                            (= current-slide 1)
                            (= current-slide slides-count))
                      "hovered" "")]
    [:footer
     {:class hover-class}
     [:div gist]
     [:div author]
     [:div date]
     (when place [:div place])]))

(rum/defc loading []
  [:div.loading
   [:h2 "Initializing DB"]
   #_(mdl/spinner {:is-active true})])

(defn- setup-key-stream [store]
  (let [interval 750
        event-stream (rxt/from-event js/document EventType.KEYDOWN)
        key-stream (rxt/throttle interval event-stream)
        emit-fn #(ptk/emit! store (events/->KeyPressed (.-keyCode %)))]
    (rxt/on-value key-stream emit-fn)))

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
             (footer store deck gist current-slide)]])
         (do
           (setup-key-stream store)
           (loading)))
       (gist-form store err))]))
