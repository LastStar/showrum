(ns showrum.views.presentation
  (:require [rum.core :as rum]
            [scrum.core :as scrum]))

(rum/defc main < rum/reactive [reconciler slides]
  (let [current-slide (rum/react (scrum/subscription reconciler [:current :slide]))]
    [:div.deck
     {:style {:width     (str (count slides) "00vw")
              :transform (str "translateX(-"
                              (dec current-slide) "00vw)")}}
     (for [{:keys [:db/id :slide/order :slide/type :slide/bullets
                   :slide/title :slide/text :slide/image]} (sort-by :slide/order slides)]
       [:div.slide
        {:key id :class (name type)}
        [:h1.title title]
        (case type
          :type/bullets [:ul (for [item bullets] [:li {:key item} item])]
          :type/text    [:p text]
          :type/image   [:div [:img {:src (last (re-matches #".*\((.*)\)" image))}]]
          nil)])]))

