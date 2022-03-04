(ns cljs-chess.stories.helper
  (:require [reagent.core :as reagent]))

(defn ->params [^js args]
  (js->clj args :keywordize-keys true))

(defn ->reactified [options path]
  (if (get-in options path)
    (update-in options path reagent/reactify-component)
    options))

(defn ->default [options]
  (-> options
      (->reactified [:component])
      (->reactified [:parameters :docs :page])
      clj->js))

(defn copy-function
  [f]
  (.bind f #js {}))

(defn ->story
  "Copy a function and modify the default arguments.
  Used to create stories that have default configurations."
  [template default-values]
  (doto (copy-function template)
    (set! -args (clj->js default-values))))

(defn get-story-args
  [story]
  (js->clj (. story -args)))

(defn ->story-inputs
  [default story]
  (merge default (get-story-args story)))
