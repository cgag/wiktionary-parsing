(ns wiktionary.core
  (:require [clojure.string :as s]
            [clojure.set :as set]
            [clojure.walk :as w]
            [flatland.ordered.set :as oset]
            [flatland.ordered.map :as omap]
            [wiktionary.templates :as t]))

;; TODO: eventually consider scrapping this and contributing to
;; dbpedia's wiktionary stuff instead?

(defn fmap [f m]
  (into {} (for [[k v] m] [k (f v)]))) 
(def downcase-vals (partial fmap s/lower-case))

(def filepath "spanish-definitions.tsv")
(def lines (line-seq (clojure.java.io/reader filepath)))
(def line-vecs (map #(s/split % #"\t") lines))

(defn line-vecs->entries [line-vecs]
  (->> line-vecs
       (map #(zipmap [:lang :word :pos :def] %))
       ;; drop leading # in front of definitions
       (map #(update-in % [:def] (fn [def]
                                   (.substring def 1))))
       (map downcase-vals)))

(defonce entries (line-vecs->entries line-vecs))
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

(def template-regex #"\{\{.*?\}\}" )
(def bracket-regex  #"\[\[(.*?)\]\]" )

(defn drop-surrounding [s n]
  (.substring s n (- (count s) n)))

(defn templates [definition]
  (->> (re-seq template-regex definition)
       (map #(drop-surrounding % 2))
       (map #(s/split % #"\|"))))

(defn drop-templates [def]
  (s/replace def template-regex ""))

(defn strip-brackets [def]
  (s/replace def bracket-regex (fn [[match group]] group)))

(defn verb-form-templates [entry]
  (let [verb-form-template? (fn [template]
                              (= (first template)
                                 "es-verb form of"))]
    (->> entry
         :def
         templates
         (filter verb-form-template?))))

(defn infinitives [verb-entries]
  (->> verb-entries
       (mapcat verb-form-templates)
       (map last)
       distinct
       seq))

;; TODO: don't strip templates?  Handle the reflexive template at least?
(defn definitions [entries]
  (->> entries
       (map :def)
       (map drop-templates)
       (map strip-brackets)
       (map #(s/trim %))
       (remove s/blank?)
       seq))


;;;; TODO: figure out how to parse nouns. Need to handle feminine as
;;;; well as plurals.  Is the position of the root in the template consistent?
(defn root-forms [entries]
  (->> entries
       (map :def)
       (mapcat templates)
       (keep t/parse-template)
       (apply merge)))

;; TODO: want to be able to pretend have "unico" match "único"

(declare parse-nouns parse-verbs)

(defn parse-word [word]
  (let [entries (by-word word)
        nouns   (parse-nouns (nouns entries))
        verbs   (parse-verbs (verbs entries))]
    (merge
     (when nouns {:noun nouns})
     (when verbs {:verb verbs}))))

;; TODO: root forms not done at all
(defn parse-nouns [noun-entries]
  (let [defs (definitions noun-entries)
        root-forms (root-forms noun-entries)
        _ (pr root-forms)]
    (merge
     (when defs
       {:defs (vec defs)})
     root-forms
     (comment (when root-forms
                {:root-forms (vec root-forms)})))))

(declare conjugated?)

(defn parse-verbs [verb-entries]
  (let [defs (definitions verb-entries)
        infinitives (infinitives verb-entries)]
    (merge
     (when defs
       {:defs (vec defs)})
     (when infinitives
       {:infinitives (vec infinitives)}))))

(defn conjugated? [verb-entry])