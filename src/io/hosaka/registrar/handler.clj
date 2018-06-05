(ns io.hosaka.registrar.handler
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [manifold.deferred :as d]
            [yada.yada :as yada]
            [io.hosaka.registrar.orchestrator :as orchestrator]
            [io.hosaka.registrar.user :refer [secure]]
            [io.hosaka.common.db.health :as health]))

(defn get-db-health [health {:keys [response]}]
  (->
   (health/get-health health)
   (d/chain #(if (= (:health %1) "HEALTHY")
               (assoc response :body %1 :status 200)
               (assoc response :body %1 :status 503)))))

(defn get-entity-by-id [orchestrator {:keys [response] :as ctx}]
  (let [id (-> ctx :parameters :path :id)]
    (d/catch
        (orchestrator/get-entity-by-id orchestrator id)
        (fn [e] (assoc response :body {:err "Not found"} :status 404)))))

(defn update-entity [orchestrator {:keys [body user]}]
  (d/chain
   (orchestrator/update-entity orchestrator body (:id user))
   #(if % {:stats "Updated"}  nil)))

(defn build-routes [{:keys [user health orchestrator]}]
  ["/"
   [
    ["entities"
     (yada/resource {:methods
                     {:post
                      {:produces "application/json"
                       :consumes "application/json"
                       :response (secure
                                  user
                                  #{"REGISTRAR_UPDATE_ENTITY"}
                                  (partial update-entity orchestrator))}}})]
    [["entities/" :id]
     (yada/resource {:parameters {:path {:id String}}
                     :methods
                     {:get
                      {:produces "application/json"
                       :response (partial get-entity-by-id orchestrator)}}})]
    ["health"
     (yada/resource {:methods {:get {:response (partial get-db-health health)
                                     :produces "application/json"}}})]

    ]])

(defrecord Handler [orchestrator routes health user]
  component/Lifecycle

  (start [this]
    (assoc this :routes (build-routes this)))

  (stop [this]
    (assoc this :routes nil)))

(defn new-handler []
  (component/using
   (map->Handler {})
   [:health :user :orchestrator]))

