(ns wiktionary.templates
  (:require [clojure.string :as s]))

;[{:fem "adictivo"} {:plural-of "adictivo"} {:fem}]

(declare template-functions)

(defn t-name [t]
  (first t))

(defn t-body [t]
  (rest t))

(def bracket-regex  #"\[\[(.*?)\]\]" )
(defn strip-brackets [def]
  (s/replace def bracket-regex (fn [[match group]] group)))

;; TODO: root word is the wrong terminology

;; TODO: just assuming that the first non kv pair is
;; the word we're looking for
(defn root-word [t]
  (let [body (t-body t)
        kv-pairs (map #(s/split % #"=") body)]
    (-> (filter #(= (count %) 1) kv-pairs)
        ffirst
        strip-brackets)))

(defn parse-template [t]
  (let [name (t-name t)
        template-fn (template-functions name)]
    (when template-fn
      (template-fn t))))

(declare feminine-of plural-of)

(def template-functions
  {"feminine of" feminine-of
   "plural of"   plural-of
   ;"es-verb form of" es-verb
   })

(defn feminine-of [t]
  {:feminine-of (root-word t)})

(defn plural-of [t]
  {:plural-of (root-word t)})

(defn feminine-plural-of [t])

(defn es-verb-form-of)