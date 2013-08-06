(ns wiktionary.core
  (:require [clojure.string :as s]
            [clojure.set :as set]
            [clojure.walk :as w]
            [clojure.core.reducers :as r]
            [flatland.ordered.set :as oset]
            [wiktionary.parser :as p]
            [wiktionary.homeless :as homeless]))

(set! *warn-on-reflection* true)

;; TODO: Known problems:
;;  -- Haven't finished the parse-word method, still need to properly handle
;;  merging multiple definitions into a single map
;;  -- render/show may or may not work, test it out at the repl
;;  -- Punctuation is getting parsed as words, e.g., {:word ","}
;;     -- It should probably just be ap art of the word preceding it, I don't want to deal with this shit.
;;     parsing individual words in definitions really isn't very important


;; TODO: look into dbpedia

(defonce slurped (slurp "parsed-entries"))
(defonce entries (->> (s/split slurped #"\"DIVIDER\"\n")
                      (r/map read-string)
                      homeless/foldv))

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

(defonce ord-set    (oset/into-ordered-set  entries))
(defonce pos-index  (oindex ord-set [:pos]))
(defonce word-index (oindex ord-set [:word]))

(def parts-of-speech (map :pos (keys pos-index)))

;;; return a set of entries for a given word
(defn by-pos [pos]
  (pos-index {:pos pos}))

;;; return a set of entries for a given word
(defn by-word [word]
  (word-index {:word word}))

(defn pos-filter [pos entries]
  (filter #(= (:pos %) pos) entries))

(def nouns (partial pos-filter "noun"))
(def verbs (partial pos-filter "verb"))
(def adjectives (partial pos-filter "adjective"))

(def all-adjectives (by-pos "adjective"))

;;; TODO: want to be able to pretend have "unico" match "único"
(declare parse-nouns parse-verbs)

(comment {:lang "spanish"
          :word "recuerdo"
          :verb [{:form-of "recordar" 
                  :definition ["hwtever"]} 
                 {:form-of "recordar"
                  :definition ["fuck"]}]
          :noun [[{:word "memory"}] 
                 [{:word "souvineur"} {:word  "whatever"}] ] })

(defn unique-templates [entries]
  (->> entries
       (mapcat p/templates)
       (map p/template-name)
       (into #{})))

(def verb-templates (unique-templates (verbs entries)))
(def noun-templates (unique-templates (nouns entries)))

(defn conjugated? [verb-entry]
  (let [templates (p/templates verb-entry)]
    (boolean (= (p/template-name (first templates))
                "es-verb form of"))))

(defn verb-form-info [verb-entry]
  (when (conjugated? verb-entry) 
    (p/verb-form (first (p/templates verb-entry)))))

(defn conjugation-info [verb-entry]
  (-> verb-entry p/templates first p/verb-form (dissoc :template-name)))

(defn parse-verb [verb-entry]
  (if (conjugated? verb-entry)
    {:conjugation (conjugation-info verb-entry)}
    {:definition  (:body verb-entry)}))

(defn parse-word [word]
  (let [entries (by-word word)
        basic-info (dissoc (first entries) :pos :body)
        verb-entries (verbs entries)
        noun-entries (nouns entries)
        noun-bodies (into [] (map :body noun-entries))
        verb-bodies (into [] (map :body verb-entries))]
    {:word "butts"
     :noun []
     :verb []}))

(defn merge-nouns [noun-entries])
(defn merge-verbs [verb-entries])
