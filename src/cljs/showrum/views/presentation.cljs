(ns showrum.views.presentation
  (:require [rum.core :as rum]
            [showrum.views.navigation :as navigation]
            [showrum.state :as state]))

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
        :type/bullets [:ul (for [item bullets] [:li {:key item} item])]
        :type/text    [:p text]
        :type/image   [:div [:img {:src (last (re-matches #".*\((.*)\)" image))}]]
        nil)])])

(rum/defc notes < rum/reactive [slides]
  [:div.page
   [:div.navigation
    (navigation/slides-counter (count slides))]
   [:div.notes
    {:style {:width     (str (count slides) "00vw")
             :transform (str "translateX(-" (dec (rum/react state/current-slide)) "00vw)")}}
    (for [slide (sort-by :slide/order slides)]
      (if-let [notes (:slide/notes slide)]
        [:div.note {:key (:db/id slide)} notes]
        [:div.note {:key (:db/id slide)} "No notes for this slide"]))]])

(rum/defc main [decks deck slides]
  [:div.page
   (navigation/main slides decks)
   (slider slides)])
