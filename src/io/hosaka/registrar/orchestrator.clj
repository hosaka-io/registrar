(ns io.hosaka.registrar.orchestrator
  (:require [com.stuartsierra.component :as component]
            [io.hosaka.registrar.db.entities :as entities]
            [io.hosaka.registrar.db.schemas :as schemas]
            [scjsv.core :as v]
            [manifold.deferred :as d]))

(defrecord Orchestrator [db]
  component/Lifecycle

  (start [this]
    this)

  (stop [this]
    this))

(defn new-orchestrator []
  (component/using
   (map->Orchestrator {})
   [:db]))

(defn check-schema [{:keys [schema_data] :as schema} entity]
  (d/future
    (let [validate (v/validator schema_data)]
      (assoc schema :result (validate entity)))))

(defn validate-entity [{:keys [db]} {:keys [entity_type] :as entity}]
  (d/let-flow [schemas (schemas/get-schemas-for-type db entity_type)
               results (apply d/zip (map #(check-schema % entity) schemas))]
    (if (-> results first :result nil?)
      {:result :pass :schema (-> results first :ver)}
      (if-let [ver (->> results
                        (filter (comp nil? :result))
                        first
                        :ver)]
        {:result :warn :schema ver :msg (-> results first :result)}
        {:result :fail :msg (-> results first :result)}))))

(defn update-entity [{:keys [db] :as orchestrator} entity created_by]
  (d/let-flow [result (validate-entity orchestrator entity)]
    (if (= :fail (:result result))
      (d/error-deferred "validation failed")
      (d/chain
       (entities/upsert-entity db entity created_by (:schema result))
       (fn [x] true)))))

(defn get-entity-by-id [{:keys [db]} id]
  (entities/get-entity-by-id db id))
