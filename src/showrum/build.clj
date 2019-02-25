(ns showrum.build
  (:require
   [shadow.cljs.devtools.api :as shadow]
   [clojure.java.shell :as shell]
   [clojure.tools.reader :as edn]
   [com.rpl.specter :as S]
   [showrum.build.index :as index]
   [showrum.build.styles :as styles]))


(def ^:private apps
  {:presenter
   {:title       "Showrum"
    :description "Showrum Presentation Software"
    :styles      ["button" "elevation" "layout-grid" "line-ripple" "list" "ripple" "select" "theme" "textfield" "typography"]
    :scripts     ["js/mdc.js" "js/presenter.js"]}})

(defn release
  "Release new code"
  [app]
  (let [app         (cond-> app (string? app) keyword)
        config      (shadow/get-build-config app)
        dist        (str (S/select-one [:release :output-dir] config) "/")]
    (shell/sh "yarn" "install")
    (shell/sh "rm" "-rf" dist)
    (shadow/release app)
    (shell/sh "mkdir" "-p" (str dist "css"))
    (shell/sh "mkdir" "-p" (str dist "img"))
    (doseq [style (S/select-one [app :styles] apps)]
      (shell/sh "cp" (str "./node_modules/@material/" style "/dist/mdc." style ".min.css") (str dist "css")))
    (shell/sh "cp" (S/select-one [app :logo] apps) (str dist "img"))
    (spit (str dist "index.html")
          (index/render
           {:description (S/select-one [app :description] apps)
            :title       (S/select-one [app :title] apps)
            :styles      (map #(str "css/mdc." % ".min.css")
                              (S/select-one [app :styles] apps))
            :scripts     (S/select [S/ALL :output-name]
                                   (edn/read-string
                                    (slurp (str dist "assets.edn"))))}))
    (spit (str dist "css/styles.css") styles/rendered)))

(defn development
  "Prepares development"
  [app]
  (let [app (cond-> app (string? app) keyword)
        config (shadow/get-build-config app)
        dist (str (S/select-one [:devtools :http-root] config) "/")]
    (shell/sh "yarn" "install")
    (shell/sh "rm" "-rf" dist)
    (shadow/compile app)
    (shell/sh "mkdir" "-p" (str dist "css"))
    (shell/sh "mkdir" "-p" (str dist "img"))
    (doseq [style (S/select-one [app :styles] apps)]
      (shell/sh "cp" (str "./node_modules/@material/" style "/dist/mdc." style ".min.css") (str dist "css" )))
    (shell/sh "cp"  (S/select-one [app :logo] apps) (str dist "img"))
    (spit (str dist "index.html")
          (index/render
           {:description (S/select-one [app :description] apps)
            :title (S/select-one [app :title] apps)
            :styles (map #(str "css/mdc." % ".min.css")
                         (S/select-one [app :styles] apps))
            :scripts (S/select-one [app :scripts] apps)}))
    (spit (str dist "/css/styles.css") styles/rendered)))
