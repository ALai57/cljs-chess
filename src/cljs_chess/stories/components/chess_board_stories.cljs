(ns cljs-chess.stories.components.chess-board-stories
  (:require [cljs-chess.components.drag-and-drop-board :as dnd-board]
            [cljs-chess.components.chess-piece :as chess-piece]
            [cljs-chess.chess :refer [STARTING-CHESS-BOARD]]
            [cljs-chess.stories.helper :as helper]
            [reagent.core :as reagent]))

(def ^:export default
  (helper/->default {:title     "Full Chess Board"
                     :component dnd-board/drag-and-drop-board
                     :args      {:rows 8, :cols 8, :tag "example"}}))

(def state
  (reagent/atom STARTING-CHESS-BOARD))

(defn drag-and-drop-board
  "This must be present. Don't understand why, but it doesn't work without it"
  [args]
  [dnd-board/drag-and-drop-board (-> args
                                   (helper/->params)
                                   (assoc
                                     :state     @state
                                     :item-type chess-piece/chess-piece
                                     :on-drop   (partial dnd-board/on-drop-handler state)))])

;; A "Templating" example, as an alternative to the JavaScript bind syntax explained in the Storybook docs
(defn template
  "The template is a function of arguments because Storybook understands how to
  translate arguments into interactive controls"
  [args]
  (reagent/as-element [drag-and-drop-board args]))

(def ^:export Full-Chess-Board
  (helper/->story template {}))
