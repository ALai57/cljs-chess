(ns cljs-chess.stories.components.chess-board-dnd-stories
  (:require [cljs-chess.components.chess-board-dnd :as chess-board]
            [cljs-chess.stories.helper :as helper]
            [reagent.core :as reagent]))

;; dnd-kit
(def EMPTY-ROW
  (into [] (repeat 8 nil)))

(def empty-board
  (into [] (repeat 8 EMPTY-ROW)))

(def ^:export default
  (helper/->default {:title     "Chess Board DND"
                     :component chess-board/chess-board
                     :args      {:rows 8, :cols 8, :tag "example"}}))

(defn update-key
  "Convert a keywordized version of a vector to a vector"
  [k]
  (cljs.reader/read-string (str (name k))))

(defn update-game-board
  "Required because the transforming from cljs->js and js->cljs is imperfect"
  [game-board]
  (reduce-kv (fn [acc k v]
               (assoc acc (update-key k) v))
    {}
    game-board))

;; A "Templating" example, as an alternative to the JavaScript bind syntax explained in the Storybook docs
(defn template
  "The template is a function of arguments because Storybook understands how to
  translate arguments into interactive controls"
  [args]
  (reagent/as-element [chess-board/chess-board (update (helper/->params args)
                                                 :game-board
                                                 update-game-board)]))

(def ^:export Empty-Dnd-Board
  (helper/->story template {}))



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

(def ^:export Partial-Dnd-Board
  (helper/->story template {:game-board {[0 0] BLACK-ROOK}}))

(def basic-board
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

(def ^:export Full-Dnd-Board
  (helper/->story template {:game-board basic-board}))
