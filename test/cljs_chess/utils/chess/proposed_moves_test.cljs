(ns cljs-chess.utils.chess.proposed-moves-test
  (:require [cljs-chess.utils.chess.proposed-moves :as proposed-moves]
            [cljs-chess.test-utils.chess-dsl :refer [->proposed-move
                                                     --- x--

                                                     -BN -BP -BR -BQ -BB -BK
                                                     xBN xBP xBR xBQ xBB xBK

                                                     -WN -WP -WR -WQ -WB -WK
                                                     xWN xWP xWR xWQ xWB xWK]]

            [cljs.test :as t :refer-macros [are deftest is use-fixtures testing]]))

(deftest valid-endpoint?-test
  (are [description expected board]
    (testing description
      (= expected (->> board
                       (->proposed-move -BQ)
                       (proposed-moves/valid-endpoint?))))

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

(deftest slide-blocked?-test
  (are [description expected mover board]
    (testing description
      (= expected (->> board
                       (->proposed-move mover)
                       (proposed-moves/slide-blocked?))))

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
