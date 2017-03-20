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
        #(is (= {:slide/current 1 :deck/slides-count 2}
                (ptk/update % init-state)))))
    (testing "Navigate Next Slide"
      (let [ev (sut/->NavigateNextSlide)]
        (rxt/on-value (ptk/watch ev init-state nil)
                      #(is (= (sut/->SetCurrentSlide 3) %)))))
    (testing "Navigate Previous Slide"
      (let [ev (sut/->NavigatePreviousSlide)]
        (rxt/on-value (ptk/watch ev init-state nil)
                      #(is (= (sut/->SetCurrentSlide 1) %)))))))

(deftest test-searching []
  (testing "Toggle Search Panel"
    (let [ev (sut/->ToggleSearchPanel)]
      (is (= {:search/active true}
             (ptk/update ev {:search/active false})))))
  (testing "Set Active Search Result"
    (let [ev (sut/->SetActiveSearchResult 1)]
      (is (= {:search/results-count 2 :search/result 1}
             (ptk/update ev {:search/results-count 2}))))))
