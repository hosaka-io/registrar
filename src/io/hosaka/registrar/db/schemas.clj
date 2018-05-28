(ns io.hosaka.registrar.db.schemas
  (:require [manifold.deferred :as d]
            [hugsql.core :as hugsql]
            [cheshire.core :as json]
            [com.rpl.specter :as s]
            [io.hosaka.common.db :refer [get-connection]]))

(hugsql/def-db-fns "db/sql/schemas.sql")

(defn get-schemas-for-type [db entity_type]
  (d/chain
   (d/future (get-schemas-for-type-sql (get-connection db) {:entity_type entity_type}))
   (fn [s] (s/transform [s/ALL :schema_data] #(-> % .getValue (cheshire.core/parse-string true)) s))))


