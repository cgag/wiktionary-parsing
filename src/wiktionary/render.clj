(ns wiktionary.render
  (:require [wiktionary.run-parser :as run]
            [wiktionary.parser :as p]))

(declare show-body)

(defmulti render (fn [m] (first (keys m))))

(defn show [entry]
  (let [{:keys [word lang pos body]} entry]
    (str 
      "Word: "     word "\n"
      "Language: " lang "\n"
      "PoS: "      pos  "\n"
      "Definition: " (show-body body))))

(defn- type-of [token]
  (println token)
  (first (keys token)))

(defn- show-body [body]
  (for [token body]
    (do (println token)
        (condp = (type-of token)
          :word (:word token)
          :link (-> token :link :text)))))



(comment (-> (run/n-entries 2)
             first
             show))
