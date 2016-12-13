(set-env!
 :source-paths    #{"src/cljs" "src/cljc" "src/clj"}
 :resource-paths  #{"resources"}
 :dependencies '[[adzerk/boot-cljs              "1.7.228-2" :scope "test"]
                 [adzerk/boot-reload            "0.4.13"    :scope "test"]
                 [pandeiro/boot-http            "0.7.2"     :scope "test"]
                 [com.cemerick/piggieback       "0.2.1"     :scope "test"]
                 [org.clojure/tools.nrepl       "0.2.12"    :scope "test"]
                 [weasel                        "0.7.0"     :scope "test"]
                 [org.clojure/clojurescript     "1.9.293"]
                 [rum                           "0.10.7"]
                 [rum-mdl                       "0.2.0"]
                 [datascript                    "0.15.4"]
                 [binaryage/devtools            "0.8.3"     :scope "test"]
                 [binaryage/dirac               "0.8.5"     :scope "test"]
                 [powerlaces/boot-cljs-devtools "0.1.2"     :scope "test"]
                 [markdown-clj                  "0.9.91"    :scope "test"]
                 [crisptrutski/boot-cljs-test   "0.3.0"     :scope "test"]
                 [org.martinklepsch/boot-garden "1.2.5-3"   :scope "test"]])

(require
 '[adzerk.boot-cljs              :refer [cljs]]
 '[adzerk.boot-reload            :refer [reload]]
 '[pandeiro.boot-http            :refer [serve]]
 '[powerlaces.boot-cljs-devtools :refer [cljs-devtools]]
 '[org.martinklepsch.boot-garden :refer [garden]]
 '[crisptrutski.boot-cljs-test   :refer [test-cljs]])

(deftask build []
  (comp (speak)
        (cljs)
        (sift :add-jar {'cljsjs/material #".*.css$"})
        (sift :move {#".*/material.inc.css"     "css/material.inc.css"
                     #".*/material.min.inc.css" "css/material.min.inc.css"})
        (garden :styles-var 'showrum.styles/screen
                :output-to "css/garden.css")))

(deftask run []
  (comp (serve)
        (watch)
        (reload)
        (cljs-devtools)
        (build)))

(deftask production []
  (task-options! cljs {:optimizations :advanced}
                 garden {:pretty-print false})
  identity)

(deftask development []
  (task-options! cljs {:optimizations :none
                       :source-map true}
                 reload {:on-jsload 'showrum.app/init})
  identity)

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp (development)
        (run)))

(deftask testing []
  (set-env! :source-paths #(conj % "test/cljs"))
  identity)

;;; This prevents a name collision WARNING between the test task and
;;; clojure.core/test, a function that nobody really uses or cares
;;; about.
(ns-unmap 'boot.user 'test)

(deftask test []
  (comp (testing)
        (test-cljs :js-env :phantom
                   :exit?  true)))

(deftask auto-test []
  (comp (testing)
        (watch)
        (test-cljs :js-env :phantom)))

(deftask prod
  "Simple alias to build production build"
  []
  (comp (production)
        (build)
        (target)))
