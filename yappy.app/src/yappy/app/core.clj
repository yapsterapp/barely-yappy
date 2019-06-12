(ns yappy.app.core
  (:require
   [aleph.http :as http]
   [schema.core :as s]
   [cats.core :refer [return]]
   [manifold.stream :as stream]
   [manifold.bus :as bus]
   [cheshire.core :as json]
   [yada.yada :as yada]
   [integrant.core :as ig]
   [clojure.tools.logging :refer [info warn error]]
   [prpr.promise :as prpr :refer [ddo]]))

(defn json-variant->
  "parse a json encoded variant"
  [s]
  (try
    (when-let [v (some-> s (json/parse-string true))]
      (let [[tag data] v]
        [(if-not (keyword? tag) (keyword (str tag)) tag)
         data]))
    (catch Exception e
      (error e "failure parsing client variant" s)
      nil)))

(defmethod ig/init-key ::usermsg-bus
  [_ _]
  (bus/event-bus))

(defn up-consumer
  [v]
  (info "up-consumer" v))

(defmethod ig/init-key ::websockets
  [_ {usermsg-bus :usermsg-bus}]
  (fn [{{user-id :user-id} :params
        :as req}]
    (warn ::websockets user-id)
    (ddo [ws (http/websocket-connection req)]
      (stream/consume up-consumer ws)
      (stream/connect (bus/subscribe usermsg-bus user-id)
                      ws))))

(defmethod ig/init-key ::send
  [_ {usermsg-bus :usermsg-bus}]
  (yada/resource
   {:id ::send
    :methods
    {:get
     {:parameters {:query {:msg s/Str}}
      :produces ["application/json"]
      :response (fn [{{{p-from :from-user-id
                        p-to :to-user-id} :path
                       {q-msg :msg} :query} :parameters
                      :as ctx}]
                  (ddo [:let [msg {:from p-from
                                   :to p-to
                                   :msg q-msg}]
                        _ (bus/publish! usermsg-bus
                                        p-to
                                        (json/generate-string msg))]
                    (return
                     (json/generate-string
                      [:ok msg]))))}

     :post
     {:produces ["application/json"]
      :consumes ["application/json"]
      :response
      (fn [ctx]
        "send!")}}}))
