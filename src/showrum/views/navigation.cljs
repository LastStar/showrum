(ns showrum.views.navigation
  (:require [rum.core :as rum]
            [beicon.core :as rxt]
            [potok.core :as ptk]
            [showrum.events :as events]
            [showrum.material :as material]))

(rum/defc reload-button
  [store]
  (material/Fab
   {:on-click #(ptk/emit! store (events/->ReloadPresentation))}
   "replay"))


(rum/defc slides-counter
  [current-slide slides-count]
  [:div.counter (str current-slide " / " slides-count)])

(rum/defc slide-navigation
  [store slides-count current-slide]
  [:nav.slides
   (let [disabled (= current-slide 1)]
    (material/Fab
     {:disabled disabled
      :class (when disabled "inactive")
      :on-click #(ptk/emit! store (events/->NavigatePreviousSlide))}
     "navigate_before"))
   (let [disabled (>= current-slide slides-count)]
    (material/Fab
     {:disabled disabled
      :class (when disabled "inactive")
      :on-click #(ptk/emit! store (events/->NavigateNextSlide))}
     "navigate_next"))])

(rum/defc deck-chooser
  [store decks current-deck]
  [:nav.decks
   {:width (str (count decks) "2vw")}
   (for [{:deck/keys [order title slides]} decks]
     [:div
      {:key (str current-deck order)}
      (material/Button
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
