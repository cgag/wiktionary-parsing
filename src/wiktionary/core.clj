(ns wiktionary.core 
  (:require [clojure.string :as s] 
            [clojure.set :as set]
            [clojure.walk :as w]
            [clojure.core.reducers :as r]
            [flatland.ordered.set :as oset]
            [wiktionary.parser :as p]
            [wiktionary.homeless :as homeless]
            [wiktionary.render :as render]))

(set! *warn-on-reflection* true)

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

(defn verb? [entry]
  (= "verb" (:pos entry)))

(defn noun? [entry]
  (= "noun" (:pos entry)))

(def nouns (partial filter noun?))
(def verbs (partial filter verb?))

(defn non-verbs [entries] (filter #(not= "verb" (:pos %)) entries))
(def adjectives (partial pos-filter "adjective"))

(def all-adjectives (by-pos "adjective"))

;;; TODO: want to be able to pretend have "unico" match "único"
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

;; Assumption: If a verb is conjugated, the first and only thing 
;; in the definition is the conjugation info (es-verb-form-of) template
(defn verb-form-info [verb-entry]
  (when (conjugated? verb-entry) 
    (p/verb-form (first (p/templates verb-entry)))))

(defn conjugation-info [verb-entry]
  (-> verb-entry p/templates first p/verb-form (dissoc :template-name)))

(defn parse-verb [verb-entry]
  (if (conjugated? verb-entry)
    {:conjugation (conjugation-info verb-entry)}
    {:definition  (render/show-body (:body verb-entry))}))

(defn parse-other [entry]
  {:definition (render/show-body (:body entry))})

;; TODO: need to leave part of speech in the entries
(defn parse-word [word]
  (let [entries (by-word word)
        basic-info (dissoc (first entries) :body :pos)
        verb-info  (mapv parse-verb (verbs entries))
        other-info (mapv parse-other (non-verbs entries))]
    (merge basic-info
           {:non-verbs other-info
            :verbs verb-info})))

(defn definition [word]
  (parse-word word))

;; corre -> #{correr}
;; fue -> #{ir ser}
;; recuerdo -> #{recuerdo recorder}
(defn form-of [word]
  (let [word-info (parse-word word)
        verbs (:verbs word-info)]
    (set (for [verb verbs
               :when (:conjugation verb)]
           (-> verb :conjugation :infinitive)))))

(defn non-verb? [word]
  (seq (:non-verbs (definition word))))

(defn known-word? [word]
  (let [{:keys [verbs non-verbs]} (definition word)]
    (boolean (or (seq verbs)
                 (seq non-verbs)))))

(defn lemma-frequencies [words]
  (let [verb-forms (mapcat form-of words)
        other-pos  (filter non-verb? words)
        not-found  (remove known-word? words)]
    (frequencies (concat verb-forms other-pos not-found))))


(defn words [s]
  "gimme a string, get back a seq of words"
  (let [words (s/split s #"\s+|[,.|:;-@#$%^&*+()\"“”]")]
    (for [word words
          :when (and (not (s/blank? word))
                     (not (number? (read-string word))))]
      (-> word
          s/trim
          s/lower-case))))
