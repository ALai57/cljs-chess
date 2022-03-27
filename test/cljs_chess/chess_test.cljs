(ns cljs-chess.chess-test
  (:require [cljs-chess.chess :as chess :refer [BLACK-KNIGHT BLACK-PAWN BLACK-ROOK BLACK-QUEEN
                                                WHITE-KNIGHT WHITE-PAWN WHITE-ROOK WHITE-QUEEN]]
            [cljs-chess.generators.chess-generators :as cgen]
            [cljs-chess.test-utils.chess-dsl :refer [->proposed-move
                                                     --- x--

                                                     -BN -BP -BR -BQ -BB -BK
                                                     xBN xBP xBR xBQ xBB xBK

                                                     -WN -WP -WR -WQ -WB -WK
                                                     xWN xWP xWR xWQ xWB xWK]]
            [cljs.test :as t :refer-macros [are deftest is use-fixtures testing]]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [com.gfredericks.test.chuck.properties :refer-macros [for-all]]
            [matcher-combinators.test :refer-macros [match?]]
            [taoensso.timbre :refer-macros [with-level]]))

(use-fixtures
  :once
  (fn [f]
    (with-level :warn
      (f))))

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
           (is (match? piece (get @state to))
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

    "Since its the first move, Pawn can move 2 spaces forward"
    true  [[--- -BP ---]
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

(deftest check?-test
  (testing "check? makes sense for kings"
    (is (any? (chess/check? {:piece "king"} {}))))
  (testing "check? doesn't make sense for non-kings"
    (is (thrown? js/Error (chess/check? {:piece "rook"} {})))))

(deftest check?-bishop-test
  (are [description expected board]
    (testing description
      (= expected (->> board
                       (->proposed-move -BB)
                       (:state)
                       (chess/check? -WK))))

    "Bishop cannot check vertically"
    false [[-BB --- ---]
           [--- --- ---]
           [-WK --- ---]]

    "Bishop can check diagonally"
    true  [[-BB --- ---]
           [--- --- ---]
           [--- --- -WK]]

    ))

(deftest check?-rook-test
  (are [description expected board]
    (testing description
      (= expected (->> board
                       (->proposed-move -BR)
                       (:state)
                       (chess/check? -WK))))

    "Rook can check vertically"
    true [[-BR --- ---]
          [--- --- ---]
          [-WK --- ---]]

    "Rook cannot check diagonally"
    false [[-BR --- ---]
           [--- --- ---]
           [--- --- -WK]]

    ))

#_(deftest check?-castle-test
    (are [description expected board]
      (testing description
        (= expected (->> board
                         (->proposed-move -WK)
                         (chess/valid-castle?))))

      "Can castle when not threatened"
      true  [[--- -BP --- ---]
             [--- --- --- ---]
             [-WK --- x-- -WR]]

      "Cannot castle through check"
      false [[--- -BR --- ---]
             [--- --- --- ---]
             [-WK --- x-- -WR]]

      "Cannot castle while in check"
      false [[--- --- -BB ---]
             [--- --- --- ---]
             [-WK --- x-- -WR]]
      ))
