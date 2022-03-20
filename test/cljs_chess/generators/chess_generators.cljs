(ns cljs-chess.generators.chess-generators
  (:require [cljs-chess.chess :as chess]
            [clojure.test.check.generators :as gen]))

(def gen-location
  (gen/vector gen/small-integer 2))

(def gen-piece
  (gen/map gen/simple-type-printable-equatable
           gen/simple-type-printable-equatable
           {:max-elements 40}))

(def gen-piece-type
  (gen/elements (set (map :piece (vals chess/STARTING-CHESS-BOARD)))))

(def gen-owner
  (gen/elements (set (map :owner (vals chess/STARTING-CHESS-BOARD)))))

(def gen-chess-piece
  (gen/hash-map :piece gen-piece-type
                :owner gen-owner))

(def gen-board
  (gen/fmap (partial into {})
            (gen/map gen-location gen-chess-piece
                     {:min-elements 2})))
