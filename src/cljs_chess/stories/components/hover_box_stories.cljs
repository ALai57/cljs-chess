(ns cljs-chess.stories.components.hover-box-stories
  (:require [cljs-chess.components.hover-box :as hover-box]
            [cljs-chess.stories.helper :as helper]
            [reagent.core :as reagent]))

(def ^:export default
  (helper/->default {:title     "Hover Box"
                     :component hover-box/hover-box
                     :args      {}}))

;; A "Templating" example, as an alternative to the JavaScript bind syntax explained in the Storybook docs
(defn template
  "The template is a function of arguments because Storybook understands how to
  translate arguments into interactive controls"
  [args]
  (reagent/as-element [hover-box/hover-box (helper/->params args)]))

(def ^:export Default-Hover-Box
  (helper/->story template {}))

(def ^:export Small-Hover-Box
  (helper/->story template {:dim "50px"}))
