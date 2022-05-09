(ns cljs-chess.utils.chess.proposed-moves
  (:require [cljs-chess.utils.chess.board :as chess-board]
            [cljs-chess.utils.chess.pieces :as chess-pieces]
            [cljs-chess.utils.geometry :as geom]
            [taoensso.timbre :refer-macros [infof]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn get-state
  [proposed-move]
  (:state proposed-move))

(defn get-board
  [proposed-move]
  (identity (get-state proposed-move)))

(defn to-location
  [proposed-move]
  (:new-loc proposed-move))

(defn from-piece
  [proposed-move]
  (:piece proposed-move))

(defn to-piece
  [proposed-move]
  (let [board        (get-board proposed-move)
        target-space (to-location proposed-move)]
    (chess-board/get-occupant board target-space)))

(defn from-location
  [proposed-move]
  (chess-board/find-piece-location (get-board proposed-move)
    (from-piece proposed-move)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Logic
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn end-in-friendly-space?
  [proposed-move]
  (= (chess-pieces/owner (from-piece proposed-move))
     (chess-pieces/owner (to-piece proposed-move))))

(defn end-in-enemy-space?
  "Does the move end in a space with an enemy?"
  [proposed-move]
  (when (to-piece proposed-move)
    (not= (chess-pieces/owner (from-piece proposed-move))
          (chess-pieces/owner (to-piece proposed-move)))))

(defn slide-blocked?
  [proposed-move]
  (let [board (get-board proposed-move)
        piece (from-piece proposed-move)]
    (->> (geom/path-between (from-location proposed-move)
                            (to-location proposed-move))
         (chess-board/blockers board)
         (seq)
         (some?))))

(defn valid-jump?
  [valid-geom? proposed-move]
  (and (valid-geom? (from-location proposed-move)
                    (to-location proposed-move))
       (not (end-in-friendly-space? proposed-move))))

(defn valid-slide?
  [valid-geom? proposed-move]
  (true? (and (valid-geom? (from-location proposed-move)
                           (to-location proposed-move))
              (not (slide-blocked? proposed-move))
              (not (end-in-friendly-space? proposed-move)))))


(defn belongs-to-active-player?
  [proposed-move]
  (if-let [active-player (:active-player (get-state proposed-move))]
    (= (chess-pieces/owner (from-piece proposed-move)) active-player)
    true))
