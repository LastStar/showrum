(ns showrum.app
  (:require [rum.core :as rum]
            [showrum.db :as db]))

(rum/defc slides []
  [:div
   (for [[id order type title] (db/slides)]
     (case type
       :type/header
       [:div.slide.header
        {:key id}
        [:h2.title.f1 title]]
       :type/bullets
       [:div.slide.bullets
        {:key id}
        [:h2.title.f1 title]
        [:ul
         (for [item (db/items-for id)]
           [:li item])]]))])

(rum/defc deck []
  (js/console.log (db/items-for 3))
  [:div.deck
   (slides)
   [:footer (db/author)]])

(defn init []
  (db/init)
  (rum/mount (deck) (. js/document (getElementById "container"))))
