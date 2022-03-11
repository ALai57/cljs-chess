(ns cljs-chess.chess
  (:require [taoensso.timbre :refer-macros [infof]]))

;; Example of the state contained in a reagent atom
;; {[0 0] {:piece "rook" :owner "black"}
;;  [2 4] {:piece "queen" :owner "white"}}

(def BLACK-ROOK {:piece :rook :owner :black})
(def BLACK-KNIGHT {:piece :knight :owner :black})
(def BLACK-BISHOP {:piece :bishop :owner :black})
(def BLACK-QUEEN {:piece :queen :owner :black})
(def BLACK-KING {:piece :king :owner :black})
(def BLACK-PAWN {:piece :pawn :owner :black})

(def WHITE-ROOK {:piece :rook :owner :white})
(def WHITE-KNIGHT {:piece :knight :owner :white})
(def WHITE-BISHOP {:piece :bishop :owner :white})
(def WHITE-QUEEN {:piece :queen :owner :white})
(def WHITE-KING {:piece :king :owner :white})
(def WHITE-PAWN {:piece :pawn :owner :white})

(def STARTING-CHESS-BOARD
  {[0 0] BLACK-ROOK
   [0 1] BLACK-KNIGHT
   [0 2] BLACK-BISHOP
   [0 3] BLACK-KING
   [0 4] BLACK-QUEEN
   [0 5] BLACK-BISHOP
   [0 6] BLACK-KNIGHT
   [0 7] BLACK-ROOK

   [1 0] BLACK-PAWN
   [1 1] BLACK-PAWN
   [1 2] BLACK-PAWN
   [1 3] BLACK-PAWN
   [1 4] BLACK-PAWN
   [1 5] BLACK-PAWN
   [1 6] BLACK-PAWN
   [1 7] BLACK-PAWN

   [6 0] WHITE-PAWN
   [6 1] WHITE-PAWN
   [6 2] WHITE-PAWN
   [6 3] WHITE-PAWN
   [6 4] WHITE-PAWN
   [6 5] WHITE-PAWN
   [6 6] WHITE-PAWN
   [6 7] WHITE-PAWN

   [7 0] WHITE-ROOK
   [7 1] WHITE-KNIGHT
   [7 2] WHITE-BISHOP
   [7 3] WHITE-KING
   [7 4] WHITE-QUEEN
   [7 5] WHITE-BISHOP
   [7 6] WHITE-KNIGHT
   [7 7] WHITE-ROOK})

(defn lookup-piece
  [state piece]
  (first (filter (comp (partial = piece)
                   second)
           @state)))

(defn move-piece!
  [state old-coords new-coords]
  (let [piece (get @state old-coords)]
    (infof "Moving %s from %s to %s" piece old-coords new-coords)
    (swap! state dissoc old-coords)
    (swap! state assoc new-coords piece)))
