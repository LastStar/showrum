(ns showrum.events-test
  (:require-macros [cljs.test :refer [deftest testing is are]])
  (:require [showrum.events :as sut]
            [potok.core :as ptk]
            [beicon.core :as rxt]
            [cljs.spec :as s]
            [showrum.spec]
            [cljs.test :as t :include-macros true]))

(deftest test-deck-navigation []
  (testing "Set Slides Count"
    (let [ev (sut/->SetSlidesCount 1)]
      (is (= (ptk/update ev {})
             {:deck/slides-count 1})))))

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
  (testing "Activate Search Result"
    (let [ev (sut/->ActivateSearchResult 0)
          init-state {:search/results [[1 nil 1 nil 1]]}
          evs (atom [])
          stream (ptk/watch ev init-state (rxt/empty))]
      (rxt/on-value stream (fn [event] (swap! evs conj event)))
      (is (= (count @evs) 4))
      (is (some #(= (sut/->SetCurrentDeck 1) %) @evs))
      (is (some #(= (sut/->SetCurrentSlide 1) %) @evs))
      (is (some #(= (sut/->SetSlidesCount 1) %) @evs))
      (is (some #(= (sut/->ClearSearchTerm) %) @evs)))))
