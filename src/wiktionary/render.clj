(ns wiktionary.render
  (:require [clojure.string :as s]
            [wiktionary.run-parser :as run]
            [wiktionary.parser :as p]))

(defn cleanup [s]
  (-> s
      s/trim
      (s/replace #" ([.,;:])" "$1")))

(defn show-body [body]
  (cleanup
    (s/join " " 
            (for [token body]
              (condp = (p/type-of token)
                :word (:word token)
                :link (-> token :link :text)
                :template nil)))))

(defn show [entry]
  (let [{:keys [word lang pos body]} entry]
    (str 
      "Word: "     word "\n"
      "Language: " lang "\n"
      "PoS: "      pos  "\n"
      "Definition: " (show-body body))))
