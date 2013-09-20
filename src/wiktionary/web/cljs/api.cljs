(ns wiktionary.web.cljs.client
  (:require [cljs-http.client :as http]
            [cljs.reader :refer [read-string]]
            [cljs.core.async :refer [chan >! <! close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn word-info [word]
  (go 
    (read-string 
      (:body (<! (http/get "/edn/word-info" 
                           {:query-params {:word word}}))))))

(defn lemma-frequencies [text]
  (go
    (read-string
      (:body (<! (http/post "/edn/frequencies" 
                            {:query-params {:text text}}))))))

