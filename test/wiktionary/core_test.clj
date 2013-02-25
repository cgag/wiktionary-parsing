(ns wiktionary.core-test
  (:use clojure.test
        wiktionary.core))

(def a
  "
Spanish	correr	Verb	# to [[run]].
Spanish	correr	Verb	# to [[flow]].
Spanish	correr	Verb	# to [[chase]] away, [[drive away]].
Spanish	correr	Verb	# to [[throw out]]; to [[fire]]; to [[expel]].
Spanish	correr	Verb	# {{reflexive|lang=es}} to [[walk]] away.
Spanish	correr	Verb	# {{reflexive|lang=es|Chile}} to [[cop out]], to [[shirk]].
Spanish	correr	Verb	# to [[elapse]] (time).
Spanish	correr	Verb	# to [[go around]], [[spread]] (rumors).
Spanish	correr	Verb	# to [[rush]].
Spanish	correr	Verb	# {{reflexive|lang=es|Spain}} to have an [[orgasm]].
Spanish	correr	Noun	# {{uncountable|lang=es}} [[course]], [[passing]] {{gloss|of time}}")

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))