(ns wiktionary.web.core
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [wiktionary.web.views :as views]
            [ring.adapter.jetty :as server]))

(defroutes app-routes
  (GET "/" [] (views/home))
  (GET "/word-info" {{word :word} :params} 
       (views/word-info word))
  (POST "/frequencies" {{text :text} :params} 
        (views/word-frequencies text))
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

(comment (start-server))
(comment (stop-server))
