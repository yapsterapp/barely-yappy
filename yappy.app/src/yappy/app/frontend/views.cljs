(ns yappy.app.frontend.views
  (:require
   [re-frame.core :as re-frame]
   [yappy.app.frontend.subs :as subs]
   [yappy.app.frontend.events :as events]))

(defn main-panel []
  (let [user-id @(re-frame/subscribe [::subs/user-id])
        messages @(re-frame/subscribe [::subs/messages])]
    [:div
     [:input {:type "text"
              :placeholder "enter a user-id"
              :value user-id
              :on-change (fn [e]
                           (re-frame/dispatch
                            [::events/change-user-id
                             (-> e .-target .-value)]))}]
     [:button {:on-click #(re-frame/dispatch [::events/resubscribe-user])} "subscribe"]
     (for [{from :from
            to :to
            msg :msg} messages]
       [:div
        [:span from ": "]
        [:span msg]])
     ]))
