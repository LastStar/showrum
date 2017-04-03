(ns showrum.events-test
  (:require-macros [cljs.test :refer [deftest testing is are]])
  (:require [showrum.events :as sut]
            [potok.core :as ptk]
            [beicon.core :as rxt]
            [cljs.test :as t :include-macros true]))

(defonce fixture
  {:db/init
   {:db/decks          '({:deck/author "Josef Pospíšil"
                          :deck/date   "2017-02-20"
                          :deck/title  "Intro"
                          :deck/order  1
                          :deck/slides [{:slide/order 1
                                         :slide/type  :type/main-header
                                         :slide/title "Po Potoce ~~~~~ By the Stream ~~"}]})
    :db/index          '([1 "Intro" 1 "Po Potoce ~~~~~ By the Stream ~~" 1])
    :deck/slides-count 0}
   :response/success
   {:status 200
    :body   "---\nauthor: Josef Pospíšil\ndate: 2017-02-20\ntitle: Intro\n---\n\n# Po Potoce ~~~~~ By the Stream ~~\n\n===\n"}})

(deftest test-initialization []
  (testing "Initialize Gist"
    (let [ev (sut/->InitializeGist "http://gist.github.com/")]
      (is (= (ptk/update ev {})
             {:db/gist "http://gist.github.com/"}))))
  (testing "Set From Gist Content Failed"
    (let [ev (sut/->SetFromGistContent {:status 404})]
      (is (= (ptk/update ev {})
             {:db/error "XHR error" :db/gist nil}))))
  (testing "Set From Gist Content Success"
    (let [ev (sut/->SetFromGistContent (:response/success fixture))]
      (is (= (ptk/update ev {})
             (:db/init fixture)))
      (rxt/on-value (ptk/watch ev {} (rxt/empty))
                    #(is (= (sut/->NavigateUrl) %)))))
  (testing "Reload Presentation"
    (let [ev (sut/->ReloadPresentation)]
      (rxt/on-value (ptk/watch ev {:db/gist "http://gist.github.com/"} (rxt/empty))
                    #(is (= (sut/->InitializeGist "http://gist.github.com/") %))))))

(deftest test-deck-navigation []
  (testing "Set Slides Count"
    (let [ev (sut/->SetSlidesCount 1)]
      (is (= (ptk/update ev {})
             {:deck/slides-count 1}))))
  (testing "Set Current Deck"
    (let [ev (sut/->SetCurrentDeck 1)]
      (is (= (ptk/update ev {})
             {:deck/current 1}))
      (rxt/on-value (ptk/watch ev {} (rxt/empty))
                    #(is (= (sut/->NavigateUrl) %)))))
  (testing "Init Deck"
    (let [ev (sut/->InitDeck 1)
          evs (atom #{})]
      (rxt/on-value (ptk/watch ev (:db/init fixture) (rxt/empty))
                    #(swap! evs conj %))
      (is (= (count @evs) 3))
      (is (@evs (sut/->SetCurrentDeck 1)))
      (is (@evs (sut/->SetCurrentSlide 1)))
      (is (@evs (sut/->SetSlidesCount 1)))))
  (testing "Navigate Next Deck"
    (let [ev (sut/->NavigateNextDeck)]
      (rxt/on-value (ptk/watch ev {:deck/current 1} (rxt/empty))
                    #(is (= (sut/->InitDeck 2))))))
  (testing "Navigate Previous Deck"
    (let [ev (sut/->NavigatePreviousDeck)]
      (rxt/on-value (ptk/watch ev {:deck/current 3} (rxt/empty))
                    #(is (= (sut/->InitDeck 2)))))))

(deftest test-slide-navigation []
  (let [init-state {:slide/current 2 :deck/slides-count 3}]
    (testing "Set Current Slide"
      (let [ev (sut/->SetCurrentSlide 1)]
        (is (= (ptk/update ev init-state)
               {:slide/current 1 :deck/slides-count 3}))))
    (testing "Navigate Next Slide"
      (let [ev (sut/->NavigateNextSlide)]
        (rxt/on-value (ptk/watch ev init-state (rxt/empty))
                      (fn [event] (is (= event (sut/->SetCurrentSlide 3)))))))
    (testing "Navigate Previous Slide"
      (let [ev (sut/->NavigatePreviousSlide)]
        (rxt/on-value (ptk/watch ev init-state (rxt/empty))
                      (fn [event] (is (= event (sut/->SetCurrentSlide 1)))))))))

(deftest test-searching []
  (testing "Toggle Search Panel"
    (let [ev (sut/->ToggleSearchPanel)]
      (is (= (ptk/update ev {:search/active false})
             {:search/active true}))))
  (testing "Set Active Search Result"
    (let [ev (sut/->SetActiveSearchResult 1)]
      (is (= (ptk/update ev {:search/results-count 2})
             {:search/results-count 2 :search/result 1}))))
  (testing "Clear Search Term"
    (let [ev (sut/->ClearSearchTerm)]
      (is (= (ptk/update ev {})
             {:search/term "" :search/active false}))))
  (testing "Init Search Navigation"
    (let [ev (sut/->InitSearchNavigation "term")]
      (is (= (ptk/update ev {})
             {:search/term "term" :search/result 0}))))
  (testing "Navigate Next Search Result"
    (let [ev (sut/->NavigateNextSearchResult)]
      (rxt/on-value (ptk/watch ev {:search/result 0} (rxt/empty))
                    #(is (= (sut/->SetActiveSearchResult 1) %)))))
  (testing "Navigate Previous Search Result"
    (let [ev (sut/->NavigatePreviousSearchResult)]
      (rxt/on-value (ptk/watch ev {:search/result 2} (rxt/empty))
                    #(is (= (sut/->SetActiveSearchResult 1) %)))))
  (testing "Set Search Term"
    (let [ev (sut/->SetSearchTerm "term")
          evs (atom #{})]
      (rxt/on-value (ptk/watch ev (:db/init fixture) (rxt/empty))
                    #(swap! evs conj %))
      (is (= (count @evs) 2))
      (is (@evs (sut/->InitSearchNavigation "term")))
      (is (@evs (sut/->SetSearchResults [])))))
  (testing "Activate Search Result"
    (let [ev (sut/->ActivateSearchResult 0)
          init-state {:search/results [[1 nil 1 nil 1]]}
          evs (atom #{})
          stream (ptk/watch ev init-state (rxt/empty))]
      (rxt/on-value stream #(swap! evs conj %))
      (is (= (count @evs) 4))
      (is (@evs (sut/->SetCurrentDeck 1)))
      (is (@evs (sut/->SetCurrentSlide 1)))
      (is (@evs (sut/->SetSlidesCount 1)))
      (is (@evs (sut/->ClearSearchTerm))))))

                                        ;FIXME: add tests for KeyPressed, RouteMatched
