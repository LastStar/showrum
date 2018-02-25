(ns showrum.views
  (:require [rum.core :as rum]
            [beicon.core :as rxt]
            [potok.core :as ptk]
            [showrum.events :as events]
            [showrum.material :as material]
            [showrum.views.navigation :as navigation]
            [showrum.views.search :as search]
            [showrum.views.presentation :as presentation])
  (:import goog.events.EventType))

(rum/defc gist-form [store err]
  (let [submit-fn (fn [e]
                    (.preventDefault e)
                    (ptk/emit! store
                               (events/->InitializeGist
                                (-> e .-target (aget "gist") .-value))))]
    [:div.gist
     [:h4 "No decks loaded. Please add uri for the gist."]
     [:h5
      {:style {:color :red}}
      (when err (str "There was a " err))]
     [:form
      {:on-submit submit-fn}
      [:div
       (material/TextField
        {:required true :default-value name :name "gist" :style {:width "50rem"}}
        "Gist")]
      [:div (material/Button {} "Parse")]]]))


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
   [:h2 "Initializing DB"]])


(defn- setup-key-stream [store]
  (let [interval     750
        event-stream (rxt/from-event js/document EventType.KEYDOWN)
        key-stream   (rxt/throttle interval event-stream)
        emit-fn      #(ptk/emit! store (events/->KeyPressed (.-keyCode %)))]
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
