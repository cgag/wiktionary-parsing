(ns wiktionary.template
  (:refer-clojure :exclude [name]))

(set! *warn-on-reflection* true)

(defn name [t]
  (get t 0))

(defn verb-form [t]
  {:template-name    (name t)
   :infinitive (get t 1)
   :number  (get t "number")
   :formal? (get t "formal")
   :person  (get t "pers")
   :sense   (get t "sense")
   :mood    (get t "mood")
   :ending  (get t "ending")})

;; TODO: Need to be able to parse templates to get info like: the infinitive of conjugated verbs,
;; masculinity, plural/singular, etc
;; TODO: Dump a list of template names and make sure we know what's available before we start 
;; doing anything with the information.
;;

(comment (verb-form 
           {1 "abacorar",
            "number" "plural",
            "formal" "yes",
            "pers" "2",
            "sera" "ra",
            "tense" "imperfect",
            "mood" "subjunctive",
            "ending" "ar",
            0 "es-verb form of"}))
