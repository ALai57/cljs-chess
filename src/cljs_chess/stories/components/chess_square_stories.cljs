(ns cljs-chess.stories.components.chess-square-stories
  (:require [cljs-chess.components.chess-square :as chess-square]
            [cljs-chess.stories.helper :as helper]
            [reagent.core :as reagent]))

(def ^:export default
  (helper/->default {:title     "Chess Square"
                     :component chess-square/chess-square
                     :args      {}}))

;; A "Templating" example, as an alternative to the JavaScript bind syntax explained in the Storybook docs
(defn template
  "The template is a function of arguments because Storybook understands how to
  translate arguments into interactive controls"
  [args]
  (reagent/as-element [chess-square/chess-square (helper/->params args)]))

(def ^:export Empty-Chess-Square-Box
  (helper/->story template {}))

(def ^:export White-Rook-Chess-Square-Box
  (helper/->story template {:piece {:piece :rook
                                    :owner :white}}))

(def ^:export Black-King-Chess-Square-Box
  (helper/->story template {:piece {:piece :king
                                    :owner :black}}))

(def ^:export Small-Chess-Square-Box
  (helper/->story template {:dim "50px"}))
