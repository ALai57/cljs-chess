(ns cljs-chess.test-utils.chess-dsl
  (:require [cljs-chess.chess :refer [BLACK-KNIGHT BLACK-PAWN BLACK-ROOK BLACK-QUEEN
                                      WHITE-KNIGHT WHITE-PAWN WHITE-ROOK WHITE-QUEEN]]))


;; Used to indicate a   Used to indicate a piece that is also a
;;  piece that is not    the target square for a movement
;;  a target square
(def -BN BLACK-KNIGHT) (def xBN (with-meta -BN {:target? true}))
(def -BP BLACK-PAWN)   (def xBP (with-meta -BP {:target? true}))
(def -BR BLACK-ROOK)   (def xBR (with-meta -BR {:target? true}))
(def -BQ BLACK-QUEEN)  (def xBQ (with-meta -BQ {:target? true}))

(def -WN WHITE-KNIGHT) (def xWN (with-meta -WN {:target? true}))
(def -WP WHITE-PAWN)   (def xWP (with-meta -WP {:target? true}))
(def -WR WHITE-ROOK)   (def xWR (with-meta -WR {:target? true}))
(def -WQ WHITE-QUEEN)  (def xWQ (with-meta -WQ {:target? true}))

(def --- {})           (def x-- (with-meta --- {:target? true}))

(defn target?
  [piece]
  (true? (:target? (meta piece))))

(defn ->proposed-move
  [mover board]
  (reduce (fn [acc [y x]]
            (let [piece (get-in board [y x])]
              (cond-> acc
                (= mover piece)   (assoc :piece piece)
                (target? piece)   (assoc :new-loc [y x])
                (not-empty piece) (update :state assoc [y x] piece))))
          {:state   {}
           :piece   nil
           :new-loc nil}
          (for [y (range (count board))
                x (range (count (first board)))]
            [y x])))