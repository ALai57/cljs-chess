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

;; A "Templating" example, as an alternative to the JavaScript bind syntax explained in the Storybook docs
(defn template
  "The template is a function of arguments because Storybook understands how to
  translate arguments into interactive controls"
  [args]
  (reagent/as-element [chess-board/chess-board (helper/->params args)]))

(def ^:export Empty-Dnd-Board
  (helper/->story template {}))

(def ^:export Partial-Dnd-Board
  (helper/->story template {:game-board (-> empty-board
                                          (assoc-in [0 0] {:piece :rook
                                                           :owner :black}))}))

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

(def basic-board
  [[BLACK-ROOK BLACK-KNIGHT BLACK-BISHOP BLACK-KING BLACK-QUEEN BLACK-BISHOP BLACK-KNIGHT BLACK-ROOK]
   (into [] (repeat 8 BLACK-PAWN))
   EMPTY-ROW
   EMPTY-ROW
   EMPTY-ROW
   EMPTY-ROW
   (into [] (repeat 8 WHITE-PAWN))
   [WHITE-ROOK WHITE-KNIGHT WHITE-BISHOP WHITE-QUEEN WHITE-KING WHITE-BISHOP WHITE-KNIGHT WHITE-ROOK]])

(def ^:export Full-Dnd-Board
  (helper/->story template {:game-board basic-board}))
