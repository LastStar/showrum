(ns showrum.app
  (:require [rum.core :refer [mount]]
            [showrum.views :refer [main]]))

(defn init []
  (mount (main) (. js/document (getElementById "container"))))
