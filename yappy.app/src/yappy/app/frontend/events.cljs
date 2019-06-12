(ns yappy.app.frontend.events
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [re-frame.core :as re-frame]
   [yappy.app.frontend.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   [chord.client :refer [ws-ch]]
   [cljs.core.async :refer [<! >! put! close!]]))


(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
            db/default-db))

(re-frame/reg-event-db
 ::change-user-id
 (fn-traced [db [_ user-id]]
            (assoc db :user-id user-id)))

(re-frame/reg-event-db
 ::rx-message
 (fn-traced [db [_ msg]]
            (update-in db [:messages] conj msg)))

(defn rx-messages
  [ws]
  (go-loop [{content :message :as msg} (<! ws)]
    (when msg
      (js/console.log "rx-messages" content)
      (re-frame/dispatch
       [::rx-message content])
      (recur (<! ws)))))

(defn tx-message
  [ws msg]
  (go (let [r (>! ws msg)]
        (js/console.log "tx-message" msg r))))

(defonce user-websocket (atom nil))

(re-frame/reg-fx
 ::resubscribe-user
 (fn [{user-id :user-id}]
   (js/console.log "resubscribe-user" user-id)

   (go
     (let [{ws :ws-channel
            err :error} (<! (ws-ch
                             (str "ws://localhost:2200/websockets/" user-id)
                             {:format :json-kw}))]

       (if-not err
         (do
           (reset! user-websocket ws)
           (rx-messages ws)
           (js/console.log "subscribed: " user-id))
         (js/console.log "error:" (pr-str err)))))))

(re-frame/reg-event-fx
 ::resubscribe-user
 (fn-traced [{{user-id :user-id
               :as db} :db} _]
            (js/console.log db)
            {::resubscribe-user {:user-id user-id}}))
