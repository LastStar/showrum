(ns showrum.routes
  (:require [bide.core :as router]))

(def config
  (router/router [["/presentation/:gist/:deck/:slide" :showrum/presentation]]))
