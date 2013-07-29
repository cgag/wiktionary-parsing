(ns wiktionary.core
  (:require [clojure.string :as s]
            [clojure.set :as set]
            [clojure.walk :as w]
            [clojure.core.reducers :as r]
            [flatland.ordered.set :as oset]
            [wiktionary.parser :as p]
            [wiktionary.homeless :as homeless]))

;; TODO: Known problems:
;;  -- render/show may or may not work, test it out at the repl
;;  -- Punctuation is getting parsed as words, e.g., {:word ","}
;;     -- It should probably just be ap art of the word preceding it, I don't want to deal with this shit.
;;     parsing individual words in definitions really isn't very important


;; TODO: look into dbpedia

(defonce slurped (slurp "parsed-entries"))
(defonce entries (->> (s/split slurped #"\"DIVIDER\"\n")
                      (r/map read-string)
                      homeless/fold-into-vec))

;;; TODO: pull request flatland/ordered?
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

(defonce ord-set    (into (oset/ordered-set) entries))
(defonce pos-index  (oindex ord-set [:pos]))
(defonce word-index (oindex ord-set [:word]))

(def parts-of-speech (map :pos (keys pos-index)))

;;; return a set of entries for a given word
(defn by-pos [pos]
  (pos-index {:pos pos}))

;;; return a set of entries for a given word
(defn by-word [word]
  (word-index {:word word}))

;(defn pos-filter [pos entries]
;(filter #(= (:pos %) pos) entries))

;(def nouns (partial pos-filter "noun"))
;(def verbs (partial pos-filter "verb"))
;(def adjectives (partial pos-filter "adjective"))

;(def all-adjectives (by-pos "adjective"))

;;; TODO: want to be able to pretend have "unico" match "único"
;(declare parse-nouns parse-verbs)

;(defn parse-word [word]
;(let [entries (by-word word)
;nouns   (parse-nouns (nouns entries))
;verbs   (parse-verbs (verbs entries))]
;(merge
;(when nouns {:noun nouns})
;(when verbs {:verb verbs}))))
