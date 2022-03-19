(ns cljs-chess.chess-test
  (:require [cljs-chess.chess :as chess :refer [BLACK-ROOK
                                                BLACK-KNIGHT
                                                BLACK-PAWN]]
            [cljs.test :as t :refer-macros [are deftest is]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check :as tc]
            ))

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

(def gen-loc
  (gen/let [x gen/small-integer
            y gen/small-integer]
    [x y]))

(def gen-piece
  (gen/map gen/simple-type-printable-equatable
           gen/simple-type-printable-equatable
           {:max-elements 40}))

(def pieces-are-moveable
  (prop/for-all [from  gen-loc
                 piece gen-piece
                 to    gen-loc]
    (let [state (atom {from piece})]
      (chess/move-piece! state from to)
      (= {to piece} @state))))

(deftest move-piece!-spec
  (tc/quick-check 100 pieces-are-moveable))

(deftest move-piece!-test
  (let [state (atom {[0 0] BLACK-ROOK})]
    (chess/move-piece! state [0 1] [5 5])
    (is (= {[0 0] BLACK-ROOK}
           @state))))
