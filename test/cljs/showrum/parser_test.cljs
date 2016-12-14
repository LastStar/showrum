(ns showrum.parser-test
  (:require-macros [cljs.test :refer [deftest testing is]])
  (:require [cljs.test :as t]
            [cljs.spec :as s]
            [showrum.spec]
            [showrum.parser :as parser]))

(deftest test-preamble-parsing []
  (let [preamble "---\nauthor: Pepe\ndate: 2016-12-12\ntitle: The Best\n---"
        parsed (parser/parse preamble)]
    (is parsed)
    (is (instance? cljs.core/PersistentVector parsed))
    (is (not-empty parsed))
    (is (s/valid? :showrum.spec/deck (first parsed)))))
