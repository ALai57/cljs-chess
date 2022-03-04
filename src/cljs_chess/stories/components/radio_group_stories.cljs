(ns cljs-chess.stories.components.radio-group-stories
  (:require [cljs-chess.components.radio-group :as radio-group]
            [cljs-chess.stories.helper :as helper]
            [reagent.core :as reagent]))

(def ^:export default
  (helper/->default {:title     "Radio Group"
                     :component radio-group/basic-radio-group
                     :args      {}}))

;; A "Templating" example, as an alternative to the JavaScript bind syntax explained in the Storybook docs
(defn template
  "The template is a function of arguments because Storybook understands how to
  translate arguments into interactive controls"
  [args]
  (reagent/as-element [radio-group/basic-radio-group (helper/->params args)]))

(def ^:export Default-Radio-Group
  (helper/->story template {:group-name "Gender"
                            :state      (reagent/atom "female")
                            :elements   [{:value "female"
                                          :label "Female"}
                                         {:value "male"
                                          :label "Male"}]}))

(def ^:export Different-Default
  (helper/->story template {:group-name "Gender"
                            :state      (reagent/atom "male")
                            :elements   [{:value "female"
                                          :label "Female"}
                                         {:value "male"
                                          :label "Male"}]}))
