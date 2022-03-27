(ns cljs-chess.utils.chess.pieces
  (:refer-clojure :exclude [type]))

(defn type
  [piece]
  (:piece piece))

(defn owner
  [piece]
  (:owner piece))

(defn moved?
  [piece]
  (:moved? piece))

(defn first-move?
  [piece]
  (boolean (and piece (not (moved? piece)))))

(defn king?
  [piece]
  (= "king" (type piece)))
