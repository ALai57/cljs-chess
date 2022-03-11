(ns cljs-chess.chess
  (:require [taoensso.timbre :refer-macros [infof]]))

(defn lookup-piece
  [state piece]
  (first (filter (comp (partial = piece)
                   second)
           @state)))

(defn move-piece!
  [state old-coords new-coords]
  (let [piece (get @state old-coords)]
    (infof "Moving %s from %s to %s" piece old-coords new-coords)
    (swap! state assoc new-coords piece)
    (swap! state dissoc old-coords)))
