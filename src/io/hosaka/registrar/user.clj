(ns io.hosaka.registrar.user
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [clojure.core.cache :as cache]
            [clojure.java.io :refer [reader]]
            [clojure.set :as set]
            [aleph.http :as http]
            [manifold.deferred :as d]
            [cheshire.core :as json]
            [byte-streams :as bs]))

(defrecord User [env user-cache]
  component/Lifecycle

  (start [this]
    (assoc this
           :user-cache
           (atom (cache/ttl-cache-factory {} :ttl (:user-cache-ttl env)))))

  (stop [this]
    (assoc this
           :user-cache nil)))

(defn new-user [env]
  (map->User {:env (select-keys env [:user-cache-ttl :user-url])}))

(defn parse-stream [stream]
  (with-open [rdr (reader stream)]
    (json/parse-stream rdr true)))

(defn token->user [{:keys [user-url]} token]
  (d/chain
   (http/post (str user-url "users") {:body token :headers {"content-type" "text/plain"}})
   :body
   parse-stream
   #(merge %1
           {:permissions (-> %1 :permissions set)
            :roles (-> %1 :roles set)})))

(defn get-user [{:keys [env user-cache]} token]
  (if-let [user (cache/lookup @user-cache token)]
    (d/success-deferred user)
    (d/let-flow [user (token->user env token)]
      (swap! user-cache cache/through-cache token (constantly user))
      user)))

(defn no-authorization-token [response]
  (assoc response :body {:error "No authorization token"} :status 401))

(defn secure [keys permissions handler]
  (fn [{:keys [response request] :as ctx}]
    (if-let [header (-> request :headers (get "authorization"))]
      (if-let [token (second (re-matches #"[Bb]earer: (.*)" header))]
        (-> (get-user keys token)
            (d/catch (fn [e] (log/warn "Auth failed" e) (no-authorization-token response)))
            (d/chain
             (fn [user]
               (if (empty? (set/intersection (:permissions user) permissions))
                 (assoc response :body {:error "Incorrect permissions"} :status 403)
                 (handler (assoc ctx :user user))))))
        (no-authorization-token response))
      (no-authorization-token response))))
