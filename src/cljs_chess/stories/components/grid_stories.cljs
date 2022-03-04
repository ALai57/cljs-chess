(ns cljs-chess.stories.components.grid-stories
  (:require [cljs-chess.components.grid :as grid]
            [cljs-chess.stories.helper :as helper]
            [reagent.core :as reagent]))

(def ^:export default
  (helper/->default {:title     "Grid"
                     :component grid/grid
                     :args      {:rows 5, :cols 5, :tag "example"}}))

;; A "Templating" example, as an alternative to the JavaScript bind syntax explained in the Storybook docs
(defn template
  "The template is a function of arguments because Storybook understands how to
  translate arguments into interactive controls"
  [args]
  (reagent/as-element [grid/grid (helper/->params args)]))

(def ^:export Default-Grid
  (helper/->story template {}))
