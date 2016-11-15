(ns showrum.app
  (:require [rum.core :as rum]
            [showrum.db :as db]))

(rum/defc slides [current-slide]
  (js/console.log (db/slide-by-order 1))
  [:div
   (let [[id order type title] (db/slide-by-order current-slide)]
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

(rum/defcs deck < (rum/local 1 ::current-slide)
  [state]
  (js/console.log (db/slides))
  (let [current-slide (::current-slide state)
        slides-count (count (db/slides))]
    [:div.deck
     (slides @current-slide)
     [:nav
      (when (> @current-slide 1)
        [:a.f2
         {:on-click #(swap! current-slide dec)}
         "<"])
      (when (< @current-slide slides-count)
        [:a.f2
         {:on-click #(swap! current-slide inc)}
         ">"])
      [:span (str @current-slide "/" slides-count)]]
     [:footer (db/author)]]))

(defn init []
  (db/init)
  (rum/mount (deck) (. js/document (getElementById "container"))))
