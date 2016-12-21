(ns showrum.parser-test
  (:require-macros [cljs.test :refer [deftest testing is are]])
  (:require [cljs.test :as t]
            [cljs.spec :as s]
            [showrum.spec]
            [showrum.parser :as parser]))

(deftest test-deck-parsing []
  (let [preamble "---\nauthor: Pepe\ndate: 2016-12-12\ntitle: The Best\n---"
        parsed   (parser/parse-deck preamble)]
    (is (s/valid? :showrum.spec/deck parsed))
    (are [x y] (= x y)
      "Pepe"       (:deck/author parsed)
      "2016-12-12" (:deck/date parsed)
      "The Best"   (:deck/title parsed))))

(deftest test-slide-parsing []
  (testing "Main header slide"
    (let [slide "# Main Header"
          parsed (parser/parse-slide slide)]
      (is (s/valid? :showrum.spec/slide parsed))
      (is (= (:slide/type parsed) :type/main-header))
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
      (is (s/valid? :showrum.spec/slide parsed))
      (is (= (:slide/type parsed) :type/header))
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
      (is (s/valid? :showrum.spec/slide parsed))
      (is (= (:slide/type parsed) :type/bullets))
      (is (= (:slide/title parsed) "Bullets"))
      (is (= (:slide/bullets parsed) ["first" "second"])))
    (let [slide "## Bullets\n\n* first\ncontinued\nlong\n* second\n"
          parsed (parser/parse-slide slide)]
      (is (= (:slide/bullets parsed) ["first continued long" "second"]))))
  (testing "Text"
    (let [slide "## Text\n\nlike\nreally\nlong one"
          parsed (parser/parse-slide slide)]
      (is (s/valid? :showrum.spec/slide parsed) "Valid spec")
      (is (= (:slide/type parsed) :type/text))
      (is (= (:slide/title parsed) "Text"))
      (is (= (:slide/text parsed) "like really long one"))))
  (testing "Image slide"
    (let [slide "## Image slide\n\n![Image of you](http://you.me/image.png)"
          parsed (parser/parse-slide slide)]
      (is (s/valid? :showrum.spec/slide parsed))
      (is (= (:slide/type parsed) :type/image))
      (is (= (:slide/title parsed) "Image slide"))
      (is (= (:slide/image parsed) "![Image of you](http://you.me/image.png)")))))

(deftest test-decks-parsing
  (let [decks (str "---\nauthor: Pepe\ndate: 2016-12-12\ntitle: The Best\n---\n\n"
                   "# Main Header"
                   "\n\n---\n\n"
                   "## Header"
                   "\n\n---\n\n"
                   "## Bullets\n\n* first\n* second"
                   "\n\n---\n\n"
                   "## Text\n\nlike\nreally\nlong one"
                   "\n\n---\n\n"
                   "## Image slide\n\n![Image of you](http://you.me/image.png)")
        parsed (parser/parse-decks decks)]
    (is (s/valid? :showrum.spec/decks parsed))
    (is (s/valid? :showrum.spec/deck (first parsed)))
    (doseq [slide (rest parsed)]
      (is (s/valid? :showrum.spec/slide slide)))))
