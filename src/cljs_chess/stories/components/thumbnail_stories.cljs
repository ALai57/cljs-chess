(ns cljs-chess.stories.components.thumbnail-stories
  (:require [cljs-chess.components.thumbnail :as thumbnail]
            [cljs-chess.stories.helper :as helper]
            [reagent.core :as reagent]))

(def ^:export default
  (helper/->default {:title     "Thumbnail"
                     :component thumbnail/thumbnail
                     :args      {:image-url "/images/volunteer-unverified.png"}}))

;; A "Templating" example, as an alternative to the JavaScript bind syntax explained in the Storybook docs
(defn template
  "The template is a function of arguments because Storybook understands how to
  translate arguments into interactive controls"
  [args]
  (reagent/as-element [thumbnail/thumbnail (helper/->params args)]))

(def ^:export Default-Avatar
  (helper/->story template {}))
