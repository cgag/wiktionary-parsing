(ns wiktionary.render
  (:require [wiktionary.run-parser :as run]
            [wiktionary.parser :as p]))

(set! *warn-on-reflection* true)

(declare show-body)

(defmulti render (fn [m] (first (keys m))))

(defn show [entry]
  (let [{:keys [word lang pos body]} entry]
    (str 
      "Word: "     word "\n"
      "Language: " lang "\n"
      "PoS: "      pos  "\n"
      "Definition: " (show-body body))))

(defn show-body [body]
  (apply str
         (interpose " " (for [token body]
                          (condp = (p/type-of token)
                            :word (:word token)
                            :link (-> token :link :text)
                            :template nil)))))

(comment (-> (run/n-entries 200)
             last
             show))
