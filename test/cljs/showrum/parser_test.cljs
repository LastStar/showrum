(ns showrum.parser-test
  (:require-macros [cljs.test :refer [deftest testing is]])
  (:require [cljs.test :as t]
            [cljs.spec :as s]
            [showrum.spec]
            [showrum.parser :as parser]))

(deftest test-deck-parsing []
  (let [preamble "---\nauthor: Pepe\ndate: 2016-12-12\ntitle: The Best\n---"
        parsed (parser/parse-deck preamble)]
    (is parsed)
    (is (instance? cljs.core/PersistentArrayMap parsed))
    (is (not-empty parsed))
    (is (s/valid? :showrum.spec/deck parsed))))

(deftest test-slide-parsing []
  (testing "Main header"
    (let [slide "# Main Header"
          parsed (parser/parse-slide slide)]
      (is (= (:slide/type parsed) :type/main-header))
      (is (s/valid? :showrum.spec/slide parsed))
      (is (= (:slide/title parsed) "Main Header")))
    (let [slide "\n# Main Heading\n "
          parsed (parser/parse-slide slide)]
      (is (= (:slide/title parsed) "Main Heading")))
    (let [slide "# Main Heading"
          parsed (parser/parse-slide slide)]
      (is (= (:slide/title parsed) "Main Heading"))))
  (testing "Header"
    (let [slide "## Header"
          parsed (parser/parse-slide slide)]
      (is (= (:slide/type parsed) :type/header))
      (is (s/valid? :showrum.spec/slide parsed))
      (is (= (:slide/title parsed) "Header")))
    (let [slide "## Heading"
          parsed (parser/parse-slide slide)]
      (is (= (:slide/title parsed) "Heading")))
    (let [slide "\n## Heading\n"
          parsed (parser/parse-slide slide)]
      (is (= (:slide/title parsed) "Heading"))))
  (testing "Bullets"
    (let [slide "## Bullets\n\n* first\n* second"
          parsed (parser/parse-slide slide)]
      (is (= (:slide/type parsed) :type/bullets))
      (is (s/valid? :showrum.spec/slide parsed))
      (is (= (:slide/title parsed) "Bullets"))
      (is (= (:slide/bullets parsed) ["first" "second"])))
    (let [slide "## Bullets\n\n* first\ncontinued\nlong\n* second\n"
          parsed (parser/parse-slide slide)]
      (is (= (:slide/bullets parsed) ["first continued long" "second"]))))
  (testing "Text"
    (let [slide "## Text\n\nlike\nreally\nlong one"
          parsed (parser/parse-slide slide)]
      (is (= (:slide/type parsed) :type/text))
      (is (s/valid? :showrum.spec/slide parsed) "Valid spec")
      (is (= (:slide/title parsed) "Text"))
      (is (= (:slide/text parsed) "like really long one")))))
