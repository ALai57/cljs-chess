(ns cljs-chess.components.chess-square-dnd
  (:require [taoensso.timbre :refer-macros [infof]]
            ["react-dnd" :as rdnd]))

(defn get-image
  [{:keys [piece owner]}]
  (str "images/" owner "-" piece ".svg"))

(defn -chess-square-dnd
  "Required because the `useDrag` context uses a Hook and that must be inside a
  Functional component"
  [{:keys [dim id style piece background-color]
    :or   {dim "100px"}}]
  ;;(infof "PIECE %s ACTIVE? %s" piece active?)
  (let [[{:keys [drag-active?]} y z] (rdnd/useDrag
                                       (fn []
                                         #js {:type    "KNIGHT"
                                              :collect (fn [monitor]
                                                         #_(if (. monitor isDragging)
                                                             (js/console.log "DRAGGING")
                                                             #_(js/console.log "NOT DRAGGING"))
                                                         {:drag-active? (. monitor isDragging)})}))]
    (fn []
      [:div.background-darkgrey.hover-icon.chess-square
       {:style (merge {:width            dim
                       :height           dim
                       :float            "left"
                       :background-color background-color
                       :opacity          (if drag-active? 0.5 1)}
                 style)
        :id    id}
       (when piece
         [:img {:src   (get-image piece)
                :style {:width  "100%"
                        :height "100%"}}])])))

(defn chess-square-dnd
  [m]
  [:div
   [:f> (partial -chess-square-dnd m)]])
