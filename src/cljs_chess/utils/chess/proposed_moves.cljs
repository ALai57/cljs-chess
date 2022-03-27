(ns cljs-chess.utils.chess.proposed-moves
  (:require [cljs-chess.utils.chess.board :as chess-board]
            [cljs-chess.utils.chess.pieces :as chess-pieces]
            [cljs-chess.utils.geometry :as geom]
            [taoensso.timbre :refer-macros [infof]]))

(defn valid-endpoint?
  [{:keys [state piece new-loc] :as proposed-move}]
  (not= (chess-pieces/owner piece)
        (chess-pieces/owner (chess-board/find-loc state new-loc))))

(defn end-in-enemy-space?
  "Does the move end in a space with an enemy?"
  [{:keys [state piece new-loc] :as proposed-move}]
  (when-let [owner (chess-pieces/owner (get state new-loc))]
    (not= (chess-pieces/owner piece)
          owner)))

(defn slide-blocked?
  [{:keys [state piece new-loc] :as proposed-move}]
  (let [[old-loc] (chess-board/find-piece state piece)]
    (->> (geom/path-between old-loc new-loc)
         (chess-board/blockers state)
         (seq)
         (some?))))


(defn valid-jump?
  [valid-geom? {:keys [state piece new-loc] :as proposed-move}]
  (let [[old-loc] (chess-board/find-piece state piece)]
    (and (valid-geom? old-loc new-loc)
         (valid-endpoint? proposed-move))))

(defn valid-slide?
  [valid-geom? {:keys [state piece new-loc] :as proposed-move}]
  (true? (let [[old-loc] (chess-board/find-piece state piece)]
           (and (valid-geom? old-loc new-loc)
                (not (slide-blocked? proposed-move))
                (valid-endpoint? proposed-move)))))


(defn belongs-to-active-player?
  [{:keys [state new-loc piece] :as proposed-move}]
  (if-let [active-player (:active-player state)]
    (= (chess-pieces/owner piece) active-player)
    true))
