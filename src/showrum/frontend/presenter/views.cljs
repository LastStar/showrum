(ns showrum.frontend.presenter.views
  (:require [hx.react :as hx]
            [beicon.core :as rxt]
            [potok.core :as ptk]

            [showrum.frontend.hooks :as hooks]
            [showrum.frontend.material :as material]
            [showrum.frontend.presenter.events :as events]
            [showrum.frontend.presenter.views.navigation :as navigation]
            [showrum.frontend.presenter.views.search :as search]
            [showrum.frontend.presenter.views.presentation :as presentation]))

(hx/defnc GistForm [{:keys [store err]}]
  (let [state (rxt/to-atom store)
        err   (hooks/<-derive state :db/error)]
    [:div
     {:class "gist"
      :style {:margin "1rem auto" :width "66vw"}} ;FIXME: move to styles
     [:h4 {:class "mdc-typography--headline4"} "No decks loaded. Please add uri for the gist."]
     (when err
       [:h5
        {:class "mdc-typography--headline5"
         :style {:color :red}}
        (str "There was a " err)])
     [:form
      {:on-submit (fn [e]
                    (.preventDefault e)
                    (ptk/emit! store
                               (events/->InitializeGist
                                (-> e .-target (aget "gist") .-value))))}
      (material/TextField {:name "gist" :style {:width "66vw"}} "Gist URL")
      [:div
        {:style {:text-align :right}}
        [material/Button {} "Parse"]]]]))

(hx/defnc Footer [{:keys [store deck gist current-slide]}]
  (let [state                 (rxt/to-atom store)
        {:deck/keys
         [author date place]} deck
        hovered               (hooks/<-derive state :navigation/hovered)
        slides-count          (hooks/<-derive state :deck/slides-count)
        hover-class           (if (or hovered
                                      (= current-slide 1)
                                      (= current-slide slides-count))
                                "hovered" "")]
    [:footer
     {:class hover-class}
     [:div gist]
     [:div author]
     [:div date]
     (when place [:div place])]))

(hx/defnc Loading [_]
  [:div {:class "loading"}
   [:h2 "Initializing DB"]])

(hx/defnc Decks [{store :store gist :gist}]
  (let [state                          (rxt/to-atom store)
        derive-fn                      #(select-keys % [:db/decks :deck/current :slide/current])
        {decks         :db/decks
         current-deck  :deck/current
         current-slide :slide/current} (hooks/<-derive state derive-fn)
        deck                           (and decks (some #(and (= current-deck (:deck/order %)) %) decks))
        slides                         (:deck/slides deck)]
    (if decks
      [:div
       [:div {:class "page"}
        [navigation/Main {:store store}]
        [presentation/Main {:slides slides :current-slide current-slide}]
        [Footer {:store         store :deck deck :gist gist
                 :current-slide current-slide}]]]
      [Loading])))

(hx/defnc Page [{store :store}]
  (let [state (rxt/to-atom store)
        gist  (hooks/<-derive state :db/gist)]

    [:div
     (if gist
       [Decks {:store store :gist gist}]
       [GistForm {:store store}])]))
