(ns io.hosaka.registrar.db.entities
  (:require [clojure.string :refer [join]]
            [clojure.java.jdbc :as jdbc]
            [buddy.core.hash :as hash]
            [buddy.core.codecs :refer [bytes->hex]]
            [manifold.deferred :as d]
            [hugsql.core :as hugsql]
            [cheshire.core :as json]
            [io.hosaka.common.db :refer [get-connection]]))

(hugsql/def-db-fns "db/sql/entities.sql")

(defn map->str [m]
  (cond
    (map? m) (str "{" (clojure.string/join "," (sort (map (fn [[k v]] (str (name k) ":" (map->str v))) m )))  "}")
    (set? m)  (str "[" (join "," (sort (map map->str m)))  "]")
    (coll? m) (str "[" (join "," (map map->str m))  "]")
    (string? m) (str "\"" m "\"")
    (ident? m)  (str "\"" (name m) "\"")
    :else (str m)))

(defn upsert-entity [db {:keys [id parent entity_type] :as data} created_by schema_version]
  (d/future
    (let [data_hash (-> data map->str hash/sha256 bytes->hex)
          json (json/generate-string data)
          connection (get-connection db)]
      (jdbc/with-db-transaction [tx connection]
        (upsert-entity-sql tx (assoc (select-keys data [:id :parent :entity_type]) :created_by created_by))
        (insert-entity-data-sql tx {:entity_id id
                                    :data json
                                    :created_by created_by
                                    :schema_version schema_version
                                    :entity_type entity_type
                                    :data_hash data_hash})
        (update-current-value-sql tx {:data_hash data_hash :entity_id id})))))

(defn get-entity-by-id [db id]
  (d/chain
   (d/future (get-entity-by-id-sql db {:id id}))
   :data
   #(if % (-> % .getValue (cheshire.core/parse-string true)) nil)))
