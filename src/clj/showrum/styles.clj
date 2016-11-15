(ns showrum.styles
  (:require [garden.def :refer [defrule defstyles]]
            [garden.stylesheet :refer [rule]]))

(defstyles screen
  (let [body (rule :body)]
    (body
     {:font-family "Helvetica Neue"
      :font-size   "16px"
      :line-height 1.5
      :background "linear-gradient(to left, #76b852 , #8DC26F)"
      :padding "0 1vw"
      :color "hsl(0, 0%, 30%)"}
     [:div#container
      {:position :absolute
       :top "5vh"
       :left "3vw"
       :padding "0 1rem"
       :height "90vh"
       :width "90vw"
       :background-image "linear-gradient(to top, #F4F4F4 0%, #DFDEDC 100%)"}
      [:div.deck
       [:div.slide.header
        [:h2.title
         {:margin-top "36vh"
          :margin-left "5vw"}]
        [:h3.author
         {:position :absolute
          :top "47vh"
          :left "5vw"}]]
       [:footer
        {:position :absolute
         :bottom "3vh"
         :right "2vw"}]
       [:nav
        {:position :absolute
         :top "1vh"
         :right "2vw"}
        [:a
         {:display :inline-block
          :padding "0 1rem"}]]]]
     [:h1.f4
      {:color :white}])))
