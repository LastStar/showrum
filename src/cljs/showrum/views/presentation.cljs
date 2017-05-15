(ns showrum.views.presentation
  (:require [rum.core :as rum]
            [beicon.core :as rx]))

(rum/defc main
  [slides current-slide]
  [:div.deck
   {:style {:width     (str (count slides) "00vw")
            :transform (str "translateX(-"
                            (dec current-slide) "00vw)")}}
   (for [{:slide/keys [order type bullets title text image code]} slides]
     [:div.slide
      {:key order :class (name type)}
      [:h1.title title]
      (case type
        :type/bullets [:ul (for [item bullets] [:li {:key item} item])]
        :type/text    [:p text]
        :type/image   [:div [:img {:src (last (re-matches #".*\((.*)\)" image))}]]
        :type/code    [:pre code]
        [:div])])])
