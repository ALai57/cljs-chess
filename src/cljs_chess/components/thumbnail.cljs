(ns cljs-chess.components.thumbnail)

(defn thumbnail
  [{:keys [image-url name id]}]
  [:img.avatar-thumbnail {:src  image-url
                          :name name
                          :id   id}])
