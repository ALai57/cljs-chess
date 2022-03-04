(ns cljs-chess.stories.components.secondary-button-stories
  (:require [cljs-chess.components.secondary-button :as secondary-button]
            [cljs-chess.stories.helper :as helper]
            [reagent.core :as reagent]))

(def ^:export default
  (helper/->default {:title     "Secondary Button"
                     :component secondary-button/secondary-button
                     :argTypes  {:on-click {:action "Clicked Button!"}}
                     :args      {:text "A button"}}))

;; A "Templating" example, as an alternative to the JavaScript bind syntax explained in the Storybook docs
(defn template
  "The template is a function of arguments because Storybook understands how to
  translate arguments into interactive controls"
  [args]
  (reagent/as-element [secondary-button/secondary-button (helper/->params args)]))

(def ^:export Default-Secondary-Button
  (helper/->story template {}))
