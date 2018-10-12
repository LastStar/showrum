(ns showrum.build.index
  ;FIXME: with sablono/rum
  (:require [hiccup.core :refer [html]]))


(defn render
  "Returns hiccup data for index"
  [{:keys [description title styles scripts]}]
  (html
   [:html
    [:head
     [:meta {:charset "utf8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:meta {:description description}]
     [:title title]
     (for [style styles]
       [:link {:rel "stylesheet" :href style}])
     [:link {:rel "stylesheet" :href "/css/styles.css"}]
     [:link {:rel "stylesheet" :href "https://fonts.googleapis.com/icon?family=Material+Icons"}]
     [:link {:rel "stylesheet" :href "https://fonts.googleapis.com/css?family=Roboto+Mono"}]
     [:link {:rel "stylesheet" :href "https://fonts.googleapis.com/css?family=Roboto:300,400,500"}]]
    [:body.mdc-typography
     [:div {:id "container"}]
     (for [script scripts]
       [:script {:type "text/javascript" :src script}])
     [:script
      "showrum.frontend.presenter.app.init()"]]]))
