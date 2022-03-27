(ns cljs-chess.utils.chess.board-test
  (:require [cljs-chess.utils.chess.board :as chess-board]
            [cljs-chess.chess :as chess :refer [BLACK-KNIGHT BLACK-PAWN BLACK-ROOK BLACK-QUEEN
                                                WHITE-KNIGHT WHITE-PAWN WHITE-ROOK WHITE-QUEEN]]
            [cljs.test :as t :refer-macros [are deftest is use-fixtures testing]]
            [taoensso.timbre :refer-macros [with-level]]))


(use-fixtures
  :once
  (fn [f]
    (with-level :warn
      (f))))

(deftest find-piece-test
  (are [expected piece]
    (is (= expected
           (chess-board/find-piece {[0 0] BLACK-ROOK}
                                   piece)))

    [[0 0] BLACK-ROOK] BLACK-ROOK
    nil                {:nonexistant "piece"}))

(deftest find-loc-test
  (are [expected loc]
    (is (= expected
           (chess-board/find-loc {[0 0] BLACK-ROOK}
                                 loc)))

    BLACK-ROOK [0 0]
    nil        [1 1]))

(deftest empty-square?-test
  (are [expected loc]
    (is (= expected
           (chess-board/empty-square? {[0 0] BLACK-ROOK}
                                      loc)))

    false [0 0]
    true  [1 1]))

(deftest blockers-test
  (is (= #{BLACK-KNIGHT BLACK-PAWN}
         (chess-board/blockers {[0 0] BLACK-ROOK
                                [0 1] BLACK-KNIGHT
                                [0 2] BLACK-PAWN}
                               [[0 1] [0 2]])))
  (is (= #{}
         (chess-board/blockers {[0 0] BLACK-ROOK
                                [0 1] BLACK-KNIGHT
                                [0 2] BLACK-PAWN}
                               [[1 0] [2 0]]))))
