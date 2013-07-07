(ns wiktionary.core
  (:require [clojure.string :as s]
            [clojure.set :as set]
            [clojure.walk :as w]
            [clojure.core.reducers :as r]
            [flatland.ordered.set :as oset]
            [flatland.ordered.map :as omap]
            [wiktionary.templates :as t]))

;; TODO: eventually consider scrapping this and contributing to
;; dbpedia's wiktionary stuff instead?

(defn fmap [f m]
  (into {} (for [[k v] m] [k (f v)]))) 

(def downcase-vals (partial fmap s/lower-case))

(def filepath "spanish-definitions.tsv")
                                        ;(def lines (line-seq (clojure.java.io/reader filepath)))
(defonce lines (s/split (slurp filepath) #"\n"))
(def line-vecs (r/map #(s/split % #"\t") lines))

(defn line-vecs->entries [line-vecs]
  (->> line-vecs
       (r/map #(zipmap [:lang :word :pos :def] %))
       ;; drop leading # in front of definitions
       (r/map #(update-in % [:def] (fn [def]
                                     (.substring def 1))))
       (r/map downcase-vals)))

(defonce entries (r/fold (r/monoid into vector) conj (line-vecs->entries line-vecs)))
(defonce sample  (filter (fn [_] (= (rand-int 1000) 5)) entries))

(defn oindex
  "Returns a map of the distinct values of ks in the xrel mapped to an
  ordered-set of the maps in xrel with the corresponding values of ks."
  {:added "1.0"}
  [xrel ks]
  (reduce
   (fn [m x]
     (let [ik (select-keys x ks)]
       (assoc m ik (conj (get m ik (oset/ordered-set)) x))))
   {} xrel))

(defonce ord-set (into (oset/ordered-set) entries))
(defonce pos-index  (oindex ord-set [:pos]))
(defonce word-index (oindex ord-set [:word]))

(def parts-of-speech (map :pos (keys pos-index)))

(defn by-pos [pos]
  (pos-index {:pos pos}))

;; return a set of "records"
(defn by-word [word]
  (word-index {:word word}))

(defn pos-filter [pos entries]
  (filter #(= (:pos %) pos) entries))

(defn nouns [entries]
  (pos-filter "noun" entries))

(defn verbs [entries]
  (pos-filter "verb" entries))

(defn adjectives [entries]
  (pos-filter "adjective" entries))

(def all-adjectives (by-pos "adjective"))

;; TODO: want to be able to pretend have "unico" match "único"
(declare parse-nouns parse-verbs)

(defn parse-word [word]
  (let [entries (by-word word)
        nouns   (parse-nouns (nouns entries))
        verbs   (parse-verbs (verbs entries))]
    (merge
     (when nouns {:noun nouns})
     (when verbs {:verb verbs}))))
