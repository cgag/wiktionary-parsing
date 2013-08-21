(ns wiktionary.web.core
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [wiktionary.web.views :as views]
            [ring.adapter.jetty :as server]
            [ring.util.response :as resp]))

(defroutes app-routes
  (GET "/" [] (views/home))
  (GET "/cljs" [] (views/cljs))
  (GET "/c2test" [] (views/c2-test))
  (GET "/word-info" {{word :word} :params} 
       (views/word-info word))
  (GET "/frequencies" [] (resp/redirect "/"))
  (POST "/frequencies" {{text :text} :params} 
        (views/word-frequencies text))
  (route/resources "/" {:root "wiktionary/web/resources/public"})
  (route/not-found "buts found"))


(def app (handler/site app-routes))

(def server (atom nil))

(defn start-server []
  (if @server
    (.start @server)
    (do
      (reset! server (server/run-jetty #'app {:port 8080 :join? false}))
      (start-server))))

(defn stop-server []
  (when @server
    (.stop @server)))

(comment (start-server))
(comment (stop-server))
