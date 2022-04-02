(ns cljs-chess.utils.chess.board)

(defn find-piece
  [board piece]
  (first (filter (comp (partial = piece)
                       second)
                 board)))

(defn find-piece-location
  [board piece]
  (ffirst (find-piece board-piece)))

(defn find-loc
  [board loc]
  (get board loc))

(defn blockers
  [board pts]
  (reduce (fn [acc pt]
            (if-let [blocker (find-loc board pt)]
              (conj acc blocker)
              acc))
          #{}
          pts))

(defn get-pieces
  [board]
  (map second board))

(defn empty-square?
  [board loc]
  (nil? (find-loc board loc)))

(def where-am-i (comp first find-piece))
