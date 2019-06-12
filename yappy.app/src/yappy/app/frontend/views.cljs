(ns yappy.app.frontend.views
  (:require
   [re-frame.core :as re-frame]
   [yappy.app.frontend.subs :as subs]))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1 "HelloZZZ from " @name]
     ]))
