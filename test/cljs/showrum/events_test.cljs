(ns showrum.events-test
  (:require-macros [cljs.test :refer [deftest testing is are]])
  (:require [showrum.events :as sut]
            [potok.core :as ptk]
            [beicon.core :as rxt]
            [cljs.spec :as s]
            [showrum.spec]
            [cljs.test :as t :include-macros true]))

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
             {:search/results-count 2 :search/result 1})))))
