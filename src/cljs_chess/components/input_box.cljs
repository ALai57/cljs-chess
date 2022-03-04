(ns cljs-chess.components.input-box)

(defn input-box
  [{:keys [label label-for value read-only]
    :or   {read-only false}}]
  [:dl.form-group
   [:dt [:label {:for label-for} label]]
   [:dd [:input.form-control {:type     "text"
                              :readOnly read-only
                              :value    value}]]])
