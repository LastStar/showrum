(ns showrum.frontend.presenter.views.presentation
  (:require [hx.react :as hx]))

(hx/defnc Main
  [{:keys [slides current-slide]}]
  [:div
   {:class "deck"
    :style {:width     (str (count slides) "00vw")
            :transform (str "translateX(-"
                            (dec current-slide) "00vw)")}}
   (for [{:slide/keys [order type bullets title text image code]} slides]
     [:div
      {:key order :class (str "slide " (name type))}
      [:h1 {:class "title"} title]
      (case type
        :type/bullets [:ul (for [bullet bullets] [:li {:key bullet} bullet])]
        :type/text    [:p text]
        :type/image   [:div [:img {:src (last (re-matches #".*\((.*)\)" image))}]]
        :type/code    [:pre code]
        [:div])])])
