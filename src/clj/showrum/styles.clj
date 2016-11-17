(ns showrum.styles
  (:require [garden.def :refer [defrule defstyles]]
            [garden.stylesheet :refer [rule]]))

(defstyles screen
  (let [body (rule :body)]
    (body
     {:background "linear-gradient(to left, hsla(0, 0%, 97%, 1) , hsl(0, 0%, 95%))"
      :color "hsl(0, 0%, 30%)"}
     [:.page
      [:nav
       {:display :flex
        :justify-content :space-between
        :align-items :center
        :position :absolute
        :top "1vh"
        :right "2vw"
        :padding "0.5rem"
        :width "10rem"
        :border-radius "0.25rem"
        :background "hsl(0, 0%, 90%)"
        :transition [[:all "1s"]]
        :transform "scale(0.25)"
        :transform-origin [[:top :right]]
        :opacity 0.25
        :z-index 10}
       [:&.hovered
        {:opacity 1
         :transition [[:all "250ms"]]
         :transform "scale(1)"}]
       [:button
        {:opacity 0.25}
        [:&.active
         {:opacity 1}]]
       [:span.counter
        {:font-size "1.44rem"
         :font-weight :bold}]]
      [:.deck
       {:position :absolute
        :top 0
        :left 0
        :width "100vw"
        :height "100vh"}
       [:.slide
        {:margin "10vh 10vw"
         :height "80vh"
         :display :flex
         :flex-direction :column
         :justify-content :space-around}
        [:&.bullets
         [:ul>li
          {:font-size "2.0736rem"
           :line-height "2.985rem"}]]]]
      [:footer
       {:display :flex
        :justify-content :space-between
        :position :absolute
        :bottom "1vh"
        :right "2vw"
        :width "30vw"}]])))
