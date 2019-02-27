(ns showrum.frontend.presenter.views.navigation
  (:require [hx.react :as hx]
            [beicon.core :as rxt]
            [potok.core :as ptk]

            [showrum.frontend.hooks :as hooks]
            [showrum.frontend.material :as material]
            [showrum.frontend.presenter.events :as events]))

(hx/defnc ReloadButton [{store :store}]
  [material/Button
   {:on-click #(ptk/emit! store (events/->ReloadPresentation))}
   [:i {:class "material-icons"} "replay"]])

(hx/defnc ArrowButton [{:keys [active on-click icon]}]
  [material/Button
   {:on-click (when active on-click)
    :disabled (not active)}
   [:i {:class "material-icons"} icon]])

(hx/defnc SlidesCounter [{:keys [current-slide slides-count]}]
  [:div {:class "counter"} (str current-slide " / " slides-count)])

(hx/defnc SlideNavigation [{:keys [store slides-count current-slide]}]
  [:nav{:class "slides"}
   [ArrowButton {:active (and (> current-slide 1) :active)
                 :on-click #(ptk/emit! store (events/->NavigatePreviousSlide))
                 :icon "navigate_before"}]
   [ArrowButton {:active (and (< current-slide slides-count) :active)
                 :on-click #(ptk/emit! store (events/->NavigateNextSlide))
                 :icon "navigate_next"}]])

(hx/defnc DeckChooser [{:keys [store decks current-deck]}]
  [:nav
   {:class "decks" :style {:width (str (count decks) "2vw")}}
   (for [{:deck/keys [order title slides]} decks]
     [:div
      {:key (str current-deck order)}
      [material/Button
       {:disabled (= current-deck order)
        :on-click #(ptk/emit! store (events/->InitDeck order))}
       title]])])

(hx/defnc SearchButton [{store :store}]
  (let [state (rxt/to-atom store)
        active (hooks/<-derive state :search/active)]
    [material/Button
     {:on-click #(ptk/emit! store (events/->ToggleSearchPanel))}
     [:i {:class "material-icons"} "search"]]))

(hx/defnc Main [{:keys [store slides decks current-slide]}]
  (let [state                          (rxt/to-atom store)
        decks         (hooks/<-derive state :db/decks)
        hovered       (hooks/<-derive state :navigation/hovered)
        current-slide (hooks/<-derive state :slide/current)
        slides-count  (hooks/<-derive state :deck/slides-count)
        current-deck  (hooks/<-derive state :deck/current)
        search-active (hooks/<-derive state :search/active)
        slides                         (:deck/slides current-deck)
        hover-class                    (if (or hovered
                                               (= current-slide 1)
                                               (= current-slide slides-count)
                                               search-active)
                                         "hovered" "")]
    [:div
     {:class          (str "navigation " hover-class)
      :on-mouse-enter (fn [e] (ptk/emit! store (events/->SetHover)))
      :on-mouse-leave (fn [e] (ptk/emit! store (events/->SetLeft)))}
     [:nav
      [ReloadButton {:store store}]
      [:span " "]
      [SearchButton {:store store}]]
     [DeckChooser {:store store :decks decks :current-deck current-deck}]
     [SlidesCounter {:slides-count  slides-count
                     :current-slide current-slide}]
     [SlideNavigation {:store         store :slides-count slides-count
                       :current-slide current-slide}]]))
