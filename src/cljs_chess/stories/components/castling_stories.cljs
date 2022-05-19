(ns cljs-chess.stories.components.castling-stories
  (:require [cljs-chess.chess :as chess]
            [cljs-chess.chess-constants :refer [BLACK-ROOK BLACK-KING
                                                WHITE-ROOK WHITE-KING]]
            [cljs-chess.components.chess-piece :as chess-piece]
            [cljs-chess.components.drag-and-drop-board :as dnd-board]
            [cljs-chess.stories.helper :as helper]
            [reagent.core :as reagent]))

(def ^:export default
  (helper/->default {:title     "Castling Chess Board"
                     :component dnd-board/drag-and-drop-board
                     :args      {:rows 8, :cols 8, :tag "example"}}))

(def state
  (reagent/atom {:board {[0 0] (assoc BLACK-ROOK :id "1")
                         [0 3] (assoc BLACK-KING :id "1")
                         [0 7] (assoc BLACK-ROOK :id "2")

                         [7 3] (assoc WHITE-KING :id "1")
                         [7 0] (assoc WHITE-ROOK :id "1")
                         [7 7] (assoc WHITE-ROOK :id "2")}}))

(defn drag-and-drop-board
  "This must be present. Don't understand why, but it doesn't work without it"
  [args]
  [dnd-board/drag-and-drop-board (-> args
                                     (helper/->params)
                                     (assoc
                                      :state     @state
                                      :item-type chess-piece/chess-piece
                                      :can-drop? chess/allowed-action?
                                      :on-drop   (partial chess/on-drop-handler state)))])

;; A "Templating" example, as an alternative to the JavaScript bind syntax explained in the Storybook docs
(defn template
  "The template is a function of arguments because Storybook understands how to
  translate arguments into interactive controls"
  [args]
  (reagent/as-element [drag-and-drop-board args]))

(def ^:export Castling-Example-Board
  (helper/->story template {}))
