(ns cljs-chess.chess
  (:require [taoensso.timbre :refer-macros [infof]]))

;; Example of the state contained in a reagent atom
;; {[0 0] {:piece "rook" :owner "black" :id "1"}
;;  [2 4] {:piece "queen" :owner "white" :id "1"}}

(def BLACK-ROOK {:piece "rook" :owner "black"})
(def BLACK-KNIGHT {:piece "knight" :owner "black"})
(def BLACK-BISHOP {:piece "bishop" :owner "black"})
(def BLACK-QUEEN {:piece "queen" :owner "black"})
(def BLACK-KING {:piece "king" :owner "black"})
(def BLACK-PAWN {:piece "pawn" :owner "black"})

(def WHITE-ROOK {:piece "rook" :owner "white"})
(def WHITE-KNIGHT {:piece "knight" :owner "white"})
(def WHITE-BISHOP {:piece "bishop" :owner "white"})
(def WHITE-QUEEN {:piece "queen" :owner "white"})
(def WHITE-KING {:piece "king" :owner "white"})
(def WHITE-PAWN {:piece "pawn" :owner "white"})

(def STARTING-CHESS-BOARD
  {[0 0] (assoc BLACK-ROOK :id "1" :first-move? true)
   [0 1] (assoc BLACK-KNIGHT :id "1")
   [0 2] (assoc BLACK-BISHOP :id "1")
   [0 3] (assoc BLACK-KING :id "1" :first-move? true)
   [0 4] (assoc BLACK-QUEEN :id "1")
   [0 5] (assoc BLACK-BISHOP :id "2")
   [0 6] (assoc BLACK-KNIGHT :id "2")
   [0 7] (assoc BLACK-ROOK :id "2" :first-move? true)

   [1 0] (assoc BLACK-PAWN :id "1" :first-move? true)
   [1 1] (assoc BLACK-PAWN :id "2" :first-move? true)
   [1 2] (assoc BLACK-PAWN :id "3" :first-move? true)
   [1 3] (assoc BLACK-PAWN :id "4" :first-move? true)
   [1 4] (assoc BLACK-PAWN :id "5" :first-move? true)
   [1 5] (assoc BLACK-PAWN :id "6" :first-move? true)
   [1 6] (assoc BLACK-PAWN :id "7" :first-move? true)
   [1 7] (assoc BLACK-PAWN :id "8" :first-move? true)

   [6 0] (assoc WHITE-PAWN :id "1" :first-move? true)
   [6 1] (assoc WHITE-PAWN :id "2" :first-move? true)
   [6 2] (assoc WHITE-PAWN :id "3" :first-move? true)
   [6 3] (assoc WHITE-PAWN :id "4" :first-move? true)
   [6 4] (assoc WHITE-PAWN :id "5" :first-move? true)
   [6 5] (assoc WHITE-PAWN :id "6" :first-move? true)
   [6 6] (assoc WHITE-PAWN :id "7" :first-move? true)
   [6 7] (assoc WHITE-PAWN :id "8" :first-move? true)

   [7 0] (assoc WHITE-ROOK :id "1" :first-move? true)
   [7 1] (assoc WHITE-KNIGHT :id "1")
   [7 2] (assoc WHITE-BISHOP :id "1")
   [7 3] (assoc WHITE-KING :id "1" :first-move? true)
   [7 4] (assoc WHITE-QUEEN :id "1")
   [7 5] (assoc WHITE-BISHOP :id "2")
   [7 6] (assoc WHITE-KNIGHT :id "2")
   [7 7] (assoc WHITE-ROOK :id "2" :first-move? true)})

(defn lookup-piece
  [state piece]
  (first (filter (comp (partial = piece)
                   second)
           state)))

(defn lookup-loc
  [state loc]
  (get state loc))

(defn update-when
  [m k f]
  (if (contains? m k)
    (update m k f)
    m))

(defn first-move?
  [piece]
  (get piece :first-move?))

(defn move-piece!
  [state old-coords new-coords]
  (let [piece (get @state old-coords)]
    (infof "Moving %s from %s to %s" piece old-coords new-coords)
    (swap! state dissoc old-coords)
    (swap! state assoc new-coords (update-when piece :first-move? (constantly false)))))

(defn piece-type
  [piece]
  (:piece piece))

(defn piece-owner
  [piece]
  (:owner piece))
