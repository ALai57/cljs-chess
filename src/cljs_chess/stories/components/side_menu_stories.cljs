(ns cljs-chess.stories.components.side-menu-stories
  (:require [cljs-chess.components.side-menu :as side-menu]
            [cljs-chess.stories.helper :as helper]
            [reagent-mui.components :refer [button]]
            [reagent.core :as reagent]))

(defn expand-button
  [props]
  [button props "Click me"])

(def ^:export default
  (helper/->default {:title     "Side Menu"
                     :component side-menu/side-menu
                     :args      {:expand-button expand-button}}))

;; A "Templating" example, as an alternative to the JavaScript bind syntax explained in the Storybook docs
(defn template
  "The template is a function of arguments because Storybook understands how to
  translate arguments into interactive controls"
  [args]
  (reagent/as-element [side-menu/side-menu (helper/->params args)]))

(def ^:export Default-Side-Menu
  (helper/->story template {}))
