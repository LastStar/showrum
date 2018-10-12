(ns showrum.frontend.presenter.views.navigation
  (:require [rum.core :as rum]
            [beicon.core :as rxt]
            [potok.core :as ptk]
            [mdc-rum.core :as mdc]
            [mdc-rum.components :as mdcc]
            [showrum.frontend.presenter.events :as events]))

(rum/defc reload-button
  [store]
  (mdcc/button
   {:on-click #(ptk/emit! store (events/->ReloadPresentation))}
   [mdc/icon "replay"]))

(defn- button
  [active event icon]
  (mdcc/button
   {:on-click (when active event)
    :disabled (not active)}
   [mdc/icon icon]))

(rum/defc slides-counter
  [current-slide slides-count]
  [:div.counter (str current-slide " / " slides-count)])

(rum/defc slide-navigation
  [store slides-count current-slide]
  [:nav.slides
   (button (and (> current-slide 1) :active)
           #(ptk/emit! store (events/->NavigatePreviousSlide))
           "navigate_before")
   (button (and (< current-slide slides-count) :active)
           #(ptk/emit! store (events/->NavigateNextSlide))
           "navigate_next")])

(rum/defc deck-chooser
  [store decks current-deck]
  [:nav.decks
   {:width (str (count decks) "2vw")}
   (for [{:deck/keys [order title slides]} decks]
     [:div
      {:key (str current-deck order)}
      (mdcc/button
       {:disabled (= current-deck order)
        :on-click #(ptk/emit! store (events/->InitDeck order))}
       title)])])

(rum/defc main < rum/reactive
  [store slides decks search-button current-slide]
  (let [state         (rxt/to-atom store)
        hovered       (rum/react (rum/cursor state :navigation/hovered))
        slides-count  (rum/react (rum/cursor state :deck/slides-count))
        current-deck  (rum/react (rum/cursor state :deck/current))
        search-active (rum/react (rum/cursor state :search/active))
        hover-class   (if (or hovered
                              (= current-slide 1)
                              (= current-slide slides-count)
                              search-active)
                        "hovered" "")]
    [:div.navigation
     {:class          hover-class
      :on-mouse-enter (fn [e] (ptk/emit! store (events/->SetHover)))
      :on-mouse-leave (fn [e] (ptk/emit! store (events/->SetLeft)))}
     [:nav
      (reload-button store)
      [:span " "]
      (search-button)]
     (deck-chooser store decks current-deck)
     (slides-counter current-slide slides-count)
     (slide-navigation store slides-count current-slide)]))
