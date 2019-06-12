(ns yappy.app.frontend.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::user-id
 (fn [db]
   (:user-id db)))

(re-frame/reg-sub
 ::messages
 (fn [db]
   (:messages db)))
