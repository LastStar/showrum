(ns showrum.app
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [rum.core :as rum]
            [rum.mdl :as mdl]
            [showrum.db :as db]
            [showrum.state :as state]
            [showrum.events :as events]))

(rum/defc slider < rum/reactive [slides]
  [:div.deck
   {:style {:width     (str (count slides) "00vw")
            :transform (str "translateX(-"
                            (dec (rum/react state/current-slide)) "00vw)")}}
   (for [{:keys [:db/id :slide/order :slide/type :slide/bullets
                 :slide/title :slide/text :slide/image]} (sort-by :slide/order slides)]
     [:div.slide
      {:key id :class (name type)}
      [:h1.title title]
      (case type
        :type/bullets
        [:ul (for [item bullets] ^{:key item} [:li item])]
        :type/text
        [:p text]
        :type/image
        [:div [:img {:src (last (re-matches #".*\((.*)\)" image))}]]
        nil)])])

(rum/defc slides-counter < rum/reactive
  [slides-count]
  [:div.counter (str (rum/react state/current-slide) " of " slides-count)])

(rum/defc deck-navigation < rum/reactive
  [decks]
  [:nav.decks
   {:width (str (count decks) "2vw")}
   (for [[id _ title] decks]
     [:div
      {:key id}
      (mdl/button
       {:mdl      [:ripple]
        :disabled (= (rum/react state/current-deck) id)
        :on-click (fn [e] (state/set-deck id))}
       title)])])

(rum/defc slide-navigation < rum/reactive
  [slide slides-count]
  [:nav.slides
   (let [active (and (> (rum/react state/current-slide) 1) :active)]
     (mdl/button
      {:mdl      [:fab :mini-fab]
       :on-click (when active #(state/prev-slide))
       :disabled (not active)}
      (mdl/icon "navigate_before")))
   (let [active (and (< (rum/react state/current-slide) slides-count) :active)]
     (mdl/button
      {:mdl      [:fab :mini-fab]
       :on-click (when active #(state/next-slide))
       :disabled (not active)}
      (mdl/icon "navigate_next")))])

(rum/defcs reload-decks
  []
  [:nav.reload
   (mdl/button
    {:mdl      [:fab :mini-fab]
     :on-click (fn [e] (state/db-cleared))}
    (mdl/icon "refresh"))])

(rum/defcs navigation <
  rum/reactive
  (rum/local false ::hovered)
  (rum/local nil ::timer)
  [state slides decks]
  (let [hovered       (::hovered state)
        timer         (::timer state)
        slides-count  (count slides)
        clear-timer   #(when @timer (.clearTimeout js/window @timer))
        current-slide (rum/react state/current-slide)
        hover-class   (if (or @hovered (= current-slide 1) (= current-slide slides-count))
                        "hovered" "")]
    [:div.navigation
     {:class          hover-class
      :on-mouse-enter (fn [e] (clear-timer) (reset! hovered true))
      :on-mouse-leave (fn [e]
                        (clear-timer)
                        (reset! timer (.setTimeout js/window #(reset! hovered false) 2000)))}
     (reload-decks)
     (deck-navigation decks)
     (slides-counter slides-count)
     (slide-navigation slides slides-count)]))

(rum/defc footer [deck]
  (let [{:keys [:deck/author :deck/date :deck/place]} deck]
    [:footer
     [:div author]
     [:div date]
     (when [:div place])]))

(rum/defc notes < rum/reactive [slides]
  [:div.page
   [:div.navigation
    (slides-counter (count slides))]
   [:div.notes
    {:style {:width     (str (count slides) "00vw")
             :transform (str "translateX(-" (dec (rum/react state/current-slide)) "00vw)")}}
    (for [slide (sort-by :slide/order slides)]
      (if-let [notes (:slide/notes slide)]
        ^{:key (:db/id slide)}
        [:div.note
         notes]
        ^{:key (:db/id slide)}
        [:div.note
         "No notes for this slide"]))]])

(rum/defc present [decks deck slides]
  [:div.page
   (navigation slides decks)
   (slider slides)
   (footer deck)])

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

(rum/defc page < rum/reactive []
  (if (rum/react state/db-initialized?)
    (let [decks (db/decks)]
      (let [deck (db/deck (rum/react state/current-deck))
            slides (:deck/slides deck)
            hash (-> js/document .-location .-hash)]
        (if (= hash "#notes")
          (notes slides)
          (present decks deck slides))))
    (gist-form)))

(defn keyboard-loop
  "Starts keyboard loop"
  []
  (go-loop []
    (let [key (<! events/keydown-chan-events)]
      (let [slides-count (count (:deck/slides (db/deck @state/current-deck)))]
        (case (.-keyCode key)
          37 (when (> @state/current-slide 1) (state/prev-slide))
          39 (when (< @state/current-slide slides-count) (state/next-slide))
          32 (when (< @state/current-slide slides-count) (state/next-slide))
          (js/console.log key)))
      (recur))))

(defn init []
  (when-not @state/loop-running?
    (keyboard-loop)
    (state/loop-running))
  (rum/mount (page) (. js/document (getElementById "container"))))
