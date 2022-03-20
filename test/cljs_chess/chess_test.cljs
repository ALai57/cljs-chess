(ns cljs-chess.chess-test
  (:require [cljs-chess.chess :as chess :refer [BLACK-KNIGHT BLACK-PAWN BLACK-ROOK]]
            [cljs-chess.generators.chess-generators :as cgen]
            [cljs.test :as t :refer-macros [are deftest is use-fixtures]]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [com.gfredericks.test.chuck.properties :refer-macros [for-all]]
            [taoensso.timbre :refer-macros [with-level]]))

(use-fixtures
  :once
  (fn [f]
    (with-level :warn
      (f))))

(deftest lookup-piece-test
  (are [expected piece]
    (is (= expected
           (chess/lookup-piece {[0 0] BLACK-ROOK}
                               piece)))

    [[0 0] BLACK-ROOK] BLACK-ROOK
    nil                {:nonexistant "piece"}))

(deftest lookup-loc-test
  (are [expected loc]
    (is (= expected
           (chess/lookup-loc {[0 0] BLACK-ROOK}
                             loc)))

    BLACK-ROOK [0 0]
    nil        [1 1]))

(deftest empty-square?-test
  (are [expected loc]
    (is (= expected
           (chess/empty-square? {[0 0] BLACK-ROOK}
                                loc)))

    false [0 0]
    true  [1 1]))

(deftest blockers-test
  (is (= #{BLACK-KNIGHT BLACK-PAWN}
         (chess/blockers {[0 0] BLACK-ROOK
                          [0 1] BLACK-KNIGHT
                          [0 2] BLACK-PAWN}
                         [[0 1] [0 2]])))
  (is (= #{}
         (chess/blockers {[0 0] BLACK-ROOK
                          [0 1] BLACK-KNIGHT
                          [0 2] BLACK-PAWN}
                         [[1 0] [2 0]]))))


(def movement-spec
  (for-all [board cgen/gen-board
            :let  [[from piece] (first board)]
            to    (gen/such-that (partial not= from)
                                 cgen/gen-location)]
    (let [state (atom (dissoc board to))]
      (chess/move-piece! state from to)
      (and (is (nil? (get @state from))
               "`From` space should be vacated after moving")
           (is (= piece (get @state to))
               "`To` space should have the new piece after moving")))))

(deftest movement-test
  (tc/quick-check 100 movement-spec))

(def movement-to-occupied-space-spec
  (prop/for-all [board cgen/gen-board]
    (let [state (atom board)
          [from piece1] (first board)
          [to   piece2] (second board)]
      (chess/move-piece! state from to)
      (is (= (dec (count board))
             (count @state))
          "Should have one less piece on the board after moving to occupied space"))))

(deftest movement-to-occupied-space-test
  (tc/quick-check 100 movement-to-occupied-space-spec))

(def movement-to-empty-space-spec
  (prop/for-all [board cgen/gen-board]
    (let [[to]      (first board)
          [from]    (second board)
          new-board (dissoc board to)
          state     (atom new-board)]
      (chess/move-piece! state from to)
      (is (= (count new-board) (count @state))
          "Should have same number of pieces on board after moving to empty space"))))

(deftest movement-to-empty-space-test
  (tc/quick-check 100 movement-to-empty-space-spec))
