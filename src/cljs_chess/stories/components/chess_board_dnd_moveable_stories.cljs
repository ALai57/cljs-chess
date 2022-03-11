(ns cljs-chess.stories.components.chess-board-dnd-moveable-stories
  (:require [cljs-chess.components.chess-board-dnd :as chess-board]
            [cljs-chess.stories.helper :as helper]
            [reagent.core :as reagent]))

;; TODO: Extract this to "Logic layer"
;;
(def ^:export default
  (helper/->default {:title     "Chess Board DND Moveable"
                     :component chess-board/chess-board
                     :args      {:rows 8, :cols 8, :tag "example"}}))


(def state
  (reagent/atom {[0 0] {:piece "rook" :owner "black"}}))

(defn chess-component
  "This must be present. Don't understand why, but it doesn't work without it"
  [args]
  [chess-board/chess-board (-> args
                             (helper/->params)
                             (assoc
                               :game-board @state
                               :on-drop    (fn [new-coords item monitor]
                                             (let [[old-coords piece] (first @state)]
                                               (println "Dropping! From: " old-coords " To: " new-coords)
                                               (swap! state assoc new-coords piece)
                                               (swap! state dissoc old-coords)))))])

;; A "Templating" example, as an alternative to the JavaScript bind syntax explained in the Storybook docs
(defn template
  "The template is a function of arguments because Storybook understands how to
  translate arguments into interactive controls"
  [args]
  (reagent/as-element [chess-component args]))

(def ^:export Test-Dnd-Board
  (helper/->story template {}))
