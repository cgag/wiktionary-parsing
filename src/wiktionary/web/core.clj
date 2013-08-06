(ns wiktionary.web.core
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.adapter.jetty :as server]))

(defroutes app-routes
  (GET "/" [] "black triangle")
  (route/resources "/")
  (route/not-found "not found"))

(def app (handler/site app-routes))

(def server (atom nil))

(defn start-server []
  (if @server
    (.start @server)
    (do
      (reset! server (server/run-jetty app {:port 8080 :join? false}))
      (start-server))))

(defn stop-server []
  (when @server
    (.stop @server)))
