(ns cljs-chess.chess-test
  (:require [cljs-chess.chess :as chess :refer [BLACK-KNIGHT BLACK-PAWN BLACK-ROOK BLACK-QUEEN
                                                WHITE-KNIGHT WHITE-PAWN WHITE-ROOK WHITE-QUEEN]]
            [cljs-chess.generators.chess-generators :as cgen]
            [cljs-chess.test-utils.chess-dsl :refer [->proposed-move
                                                     --- x--

                                                     -BN -BP -BR -BQ -BB
                                                     xBN xBP xBR xBQ xBB

                                                     -WN -WP -WR -WQ -WB
                                                     xWN xWP xWR xWQ xWB]]
            [cljs.test :as t :refer-macros [are deftest is use-fixtures testing]]
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
      (chess/move-piece! {:state   state
                          :piece   piece
                          :new-loc to})
      (and (is (nil? (get @state from))
               "`From` space should be vacated after moving")
           (is (= piece (get @state to))
               "`To` space should have the new piece after moving")))))

(deftest movement-test
  (tc/quick-check 100 movement-spec))

(def movement-to-occupied-space-spec
  (prop/for-all [board cgen/gen-board]
    (let [state         (atom board)
          [from piece1] (first board)
          [to   piece2] (second board)]
      (chess/move-piece! {:state   state
                          :piece   piece1
                          :new-loc to})
      (is (= (dec (count board))
             (count @state))
          "Should have one less piece on the board after moving to occupied space"))))

(deftest movement-to-occupied-space-test
  (tc/quick-check 100 movement-to-occupied-space-spec))

(def movement-to-empty-space-spec
  (prop/for-all [board cgen/gen-board]
    (let [[to]         (first board)
          [from piece] (second board)
          new-board    (dissoc board to)
          state        (atom new-board)]
      (chess/move-piece! {:state   state
                          :piece   piece
                          :new-loc to})
      (is (= (count new-board) (count @state))
          "Should have same number of pieces on board after moving to empty space"))))

(deftest movement-to-empty-space-test
  (tc/quick-check 100 movement-to-empty-space-spec))

(deftest valid-endpoint?-test
  (are [description expected board]
    (testing description
      (= expected (->> board
                       (->proposed-move -BQ)
                       (chess/valid-endpoint?))))

    "Target space has piece owned by other player"
    true [[-BQ xWP ---]
          [--- --- ---]
          [--- --- ---]]

    "Target space empty"
    true [[-BQ x-- ---]
          [--- --- ---]
          [--- --- ---]]

    "Target space has piece owned by same player"
    false [[-BQ xBP ---]
           [--- --- ---]
           [--- --- ---]]
    ))

(deftest valid-knight-movement?-test
  (are [description expected board]
    (testing description
      (= expected (->> board
                       (->proposed-move -BN)
                       (chess/valid-knight-movement?))))

    "(1) Valid L"
    true [[-BN --- ---]
          [--- --- x--]
          [--- --- ---]]

    "(2) Valid L"
    true [[-BN --- ---]
          [--- --- ---]
          [--- x-- ---]]

    "Not an L"
    false [[-BN --- ---]
           [--- x-- ---]
           [--- --- ---]]))

(deftest slide-blocked?-test
  (are [description expected mover board]
    (testing description
      (= expected (->> board
                       (->proposed-move mover)
                       (chess/slide-blocked?))))

    "Queen move blocked by Knight"
    true  -BQ [[-BQ --- ---]
               [--- -BN ---]
               [--- --- x--]]

    "Queen move unblocked - no other pieces on board"
    false -BQ [[-BQ --- ---]
               [--- --- ---]
               [--- --- x--]]

    "Queen move unblocked by Knight"
    false -BQ [[-BQ --- ---]
               [--- --- -BN]
               [--- --- x--]]
    ))

(deftest valid-queen-movement?-test
  (are [description expected board]
    (testing description
      (= expected (->> board
                       (->proposed-move -BQ)
                       (chess/valid-queen-movement?))))

    "Queen can move diagonal"
    true  [[-BQ --- ---]
           [--- --- ---]
           [--- --- x--]]

    "Queen can move horizontal"
    true  [[-BQ --- x--]
           [--- --- ---]
           [--- --- ---]]

    "Queen can move vertical"
    true  [[-BQ --- ---]
           [--- --- ---]
           [x-- --- ---]]

    "Queen cannot move on a non-diagonal, non-horizontal or non-vertical"
    false [[-BQ --- ---]
           [--- --- x--]
           [--- --- ---]]
    ))

(deftest valid-rook-movement?-test
  (are [description expected board]
    (testing description
      (= expected (->> board
                       (->proposed-move -BR)
                       (chess/valid-rook-movement?))))

    "Rook can move horizontal"
    true  [[-BR --- x--]
           [--- --- ---]
           [--- --- ---]]

    "Rook can move vertical"
    true  [[-BR --- ---]
           [--- --- ---]
           [x-- --- ---]]

    "Rook cannot move diagonal"
    false [[-BR --- ---]
           [--- --- ---]
           [--- --- x--]]

    "Rook cannot move on a non-diagonal, non-horizontal or non-vertical"
    false [[-BR --- ---]
           [--- --- x--]
           [--- --- ---]]
    ))

(deftest valid-pawn-movement?-test
  (are [description expected board]
    (testing description
      (= expected (->> board
                       (->proposed-move -BP)
                       (chess/valid-pawn-movement?))))

    "Pawn can move 1 space forward"
    true  [[--- -BP ---]
           [--- x-- ---]
           [--- --- ---]]

    "Since it's not the first move, Pawn cannot move 2 spaces forward"
    false [[--- -BP ---]
           [--- --- ---]
           [--- x-- ---]]
    ))

(deftest valid-pawn-take?-test
  (are [description expected board]
    (testing description
      (= expected (->> board
                       (->proposed-move -BP)
                       (chess/valid-pawn-take?))))

    "(1) Pawn can take sideways"
    true  [[--- -BP ---]
           [xWP --- ---]
           [--- --- ---]]

    "(2) Pawn can take sideways"
    true  [[--- -BP ---]
           [--- --- xWP]
           [--- --- ---]]

    "Pawn can only take sideways in the forward direction"
    false [[--- --- ---]
           [xWP --- ---]
           [--- -BP ---]]

    "Pawn cannot take forwards"
    false [[--- -BP ---]
           [--- xWP ---]
           [--- --- ---]]
    ))

(deftest valid-bishop-movement?-test
  (are [description expected board]
    (testing description
      (= expected (->> board
                       (->proposed-move -BB)
                       (chess/valid-bishop-movement?))))

    "Bishop can move diagonal"
    true  [[-BB --- ---]
           [--- --- ---]
           [--- --- x--]]

    "Bishop cannot move horizontal"
    false [[-BB --- x--]
           [--- --- ---]
           [--- --- ---]]

    "Bishop cannot move vertical"
    false [[-BB --- ---]
           [--- --- ---]
           [x-- --- ---]]

    "Bishop cannot move on a non-diagonal, non-horizontal or non-vertical"
    false [[-BB --- ---]
           [--- --- x--]
           [--- --- ---]]
    ))
