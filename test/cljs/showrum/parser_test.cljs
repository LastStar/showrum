(ns showrum.parser-test
  (:require-macros [cljs.test :refer [deftest testing is are]])
  (:require [cljs.test :as t]
            [cljs.spec :as s]
            [showrum.spec]
            [showrum.parser :as parser]))

(deftest test-preamble-parsing []
  (let [preamble "---\nauthor: Pepe\ndate: 2016-12-12\ntitle: The Best\n---\n"
        parsed   (parser/parse-preamble preamble)]
    (is (s/valid? :showrum.spec/deck parsed))
    (are [x y] (= x y)
      "Pepe"       (:deck/author parsed)
      "2016-12-12" (:deck/date parsed)
      "The Best"   (:deck/title parsed))))

(deftest test-slide-parsing []
  (testing "Main header slide"
    (let [slide "# Main Header"
          parsed (parser/parse-slide slide 1)]
      (is (s/valid? :showrum.spec/slide parsed))
      (is (= (:slide/type parsed) :type/main-header))
      (is (= (:slide/title parsed) "Main Header"))
      (is (= (:slide/order parsed) 1)))
    (let [slide "\n# Main Heading\n "
          parsed (parser/parse-slide slide 1)]
      (is (= (:slide/title parsed) "Main Heading")))
    (let [slide "# Main Heading"
          parsed (parser/parse-slide slide 1)]
      (is (= (:slide/title parsed) "Main Heading"))))
  (testing "Header"
    (let [slide "## Header"
          parsed (parser/parse-slide slide 1)]
      (is (s/valid? :showrum.spec/slide parsed))
      (is (= (:slide/type parsed) :type/header))
      (is (= (:slide/title parsed) "Header")))
    (let [slide "## Heading"
          parsed (parser/parse-slide slide 1)]
      (is (= (:slide/title parsed) "Heading")))
    (let [slide "\n## Heading\n"
          parsed (parser/parse-slide slide 1)]
      (is (= (:slide/title parsed) "Heading"))))
  (testing "Bullets"
    (let [slide "## Bullets\n\n* first\n* second"
          parsed (parser/parse-slide slide 1)]
      (is (s/valid? :showrum.spec/slide parsed))
      (is (= (:slide/type parsed) :type/bullets))
      (is (= (:slide/title parsed) "Bullets"))
      (is (= (:slide/bullets parsed) ["first" "second"])))
    (let [slide "## Bullets\n\n* first\ncontinued\nlong\n* second\n"
          parsed (parser/parse-slide slide 1)]
      (is (= (:slide/bullets parsed) ["first continued long" "second"]))))
  (testing "Text"
    (let [slide "## Text\n\nlike\nreally\nlong one"
          parsed (parser/parse-slide slide 1)]
      (is (s/valid? :showrum.spec/slide parsed) "Valid spec")
      (is (= (:slide/type parsed) :type/text))
      (is (= (:slide/title parsed) "Text"))
      (is (= (:slide/text parsed) "like really long one"))))
  (testing "Image slide"
    (let [slide "## Image slide\n\n![Image of you](http://you.me/image.png)"
          parsed (parser/parse-slide slide 1)]
      (is (s/valid? :showrum.spec/slide parsed))
      (is (= (:slide/type parsed) :type/image))
      (is (= (:slide/title parsed) "Image slide"))
      (is (= (:slide/image parsed) "![Image of you](http://you.me/image.png)")))))

(deftest test-deck-parsing
  (testing "One Deck"
    (let [decks  (str "---\nauthor: Pepe\ndate: 2016-12-12\ntitle: The Best\n---\n\n"
                      "# Main Header"
                      "\n\n---\n\n"
                      "## Header"
                      "\n\n---\n\n"
                      "## Bullets\n\n* first\n* second"
                      "\n\n---\n\n"
                      "## Text\n\nlike\nreally\nlong one"
                      "\n\n---\n\n"
                      "## Image slide\n\n![Image of you](http://you.me/image.png)")
          parsed (parser/parse-deck decks)
          deck   (first parsed)
          slides (rest parsed)]
      (is (s/valid? :showrum.spec/decks parsed))
      (is (s/valid? :showrum.spec/deck deck))
      (is (= (:db/id deck) -1))
      (is (= (:deck/order deck) 1))
      (is (= (:deck/slides deck) [-20 -21 -22 -23 -24]))
      (doseq [slide slides]
        (is (s/valid? :showrum.spec/slide slide))
        (is (> -1 (:db/id slide)))))))

(deftest test-decks-parsing
  (testing "More decks"
    (let [docs  (str "---\nauthor: Pepe\ndate: 2016-12-12\ntitle: The Best\n---\n\n"
                      "# Main Header"
                      "\n\n---\n\n"
                      "## Header"
                      "\n\n---\n\n"
                      "## Bullets\n\n* first\n* second"
                      "\n\n---\n\n"
                      "## Text\n\nlike\nreally\nlong one"
                      "\n\n---\n\n"
                      "## Image slide\n\n![Image of you](http://you.me/image.png)"
                      "\n\n===\n\n"
                      "---\nauthor: Pepe\ndate: 2016-12-12\ntitle: The Last\n---\n\n"
                      "# Main Header"
                      "\n\n---\n\n"
                      "## Header"
                      "\n\n---\n\n"
                      "## Bullets\n\n* first\n* second"
                      "\n\n---\n\n"
                      "## Text\n\nlike\nreally\nlong one"
                      "\n\n---\n\n"
                      "## Image slide\n\n![Image of you](http://you.me/image.png)")
          parsed (parser/parse-decks docs)
          decks  (filter :deck/title parsed)
          slides (filter :slide/title parsed)]
      (is (s/valid? :showrum.spec/decks parsed))
      (is (= (count decks) 2))
      (is (= (map #(:deck/order %) decks) '(1 2)))
      (doseq [deck decks]
        (is (s/valid? :showrum.spec/deck deck)))
      (doseq [slide slides]
        (is (s/valid? :showrum.spec/slide slide))))))
