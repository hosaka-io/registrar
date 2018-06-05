(ns io.hosaka.registrar
  (:require [config.core :refer [env]]
            [clojure.tools.nrepl.server :as nrepl]
            [com.stuartsierra.component :as component]
            [manifold.deferred :as d]
            [clojure.tools.logging :as log]
            [io.hosaka.registrar.handler :refer [new-handler]]
            [io.hosaka.registrar.user :refer [new-user]]
            [io.hosaka.registrar.orchestrator :refer [new-orchestrator]]
            [io.hosaka.common.util :refer [get-port]]
            [io.hosaka.common.db :refer [new-database]]
            [io.hosaka.common.db.health :refer [new-health]]
            [io.hosaka.common.server :refer [new-server]])
  (:gen-class))

(defn init-system [env]
  (component/system-map
   :user (new-user env)
   :handler (new-handler)
   :orchestrator (new-orchestrator)
   :db (new-database "registrar" env)
   :health (new-health env)
   :server (new-server env)))

(defonce system (atom {}))

(defonce repl (atom nil))

(defn -main [& args]
  (let [semaphore (d/deferred)]
    (reset! system (init-system env))

    (swap! system component/start)
    (reset! repl (if-let [nrepl-port (get-port (:nrepl-port env))] (nrepl/start-server :port nrepl-port) nil))
    (log/info "Registrar Service booted")
    (deref semaphore)
    (log/info "Registrar Service going down")
    (component/stop @system)
    (swap! repl (fn [server] (do (if server (nrepl/stop-server server)) nil)))

    (shutdown-agents)
    ))
