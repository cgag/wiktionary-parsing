(ns wiktionary.parser-test
  (:require [wiktionary.parser :as p]
            [blancas.kern.core :refer :all]
            [blancas.kern.lexer.basic :refer :all]
            [clojure.test :refer :all]))


(def test-entry "Spanish	corran	Verb	# {{uds.}} {{es-verb form of|formal=yes|person=second-person|number=plural|sense=affirmative|mood=imperative|ending=er|correr}}")

(def correr-entry "Spanish	correr	Verb	# to [[run]].")

(def simple-template "{{hello}}")
(def simple-template-spaces "{{es-verb form of}}")
(def end-with-pipe "{{hello|}}")
(def multiple-unnamed-params "{{hello| world}}")
(def named-param "{{hello=world}}")
(def named-with-whitespace "{{named = it is true indeed   }}")
(def named-params "{{this=that| named   =   this is true | please= work}}")
(def mixed-params "{{hello.!?$#!| world | named=true| what's up}}")

(def definition "Spanish	correr	Verb	# to [[run]].")

(deftest basic-info-test
  (testing "basic info"
    (let [{:keys [lang word pos]} (value p/basic-info test-entry)]
      (are [x y] (= x y) "spanish" lang
           "corran"  word
           "verb"    pos))
    (let [{:keys [lang word pos]} (value p/basic-info "Spanish	Abadán	Proper noun	# [[Abadan]]")]
      (are [x y] (= x y) 
           "spanish" lang
           "abadán"  word
           "proper noun" pos))
    (let [{:keys [lang word pos]} (value p/basic-info "Spanish\ta otra cosa, mariposa\tPhrase\t# {{idiomatic|lang=es}} Let's change the subject, shall we?$")]
      (are [x y] (= x y) 
           "spanish" lang
           "a otra cosa, mariposa" word
           "phrase" pos))
    (testing "template's for part of speech"
      (let [{:keys [lang word pos]} (value p/basic-info "Spanish	&c.	{{abbreviation|es}}	# {{obsolete form of|etc.|lang=es}}")]
        (is (= "abbreviation" pos))))))

(deftest template-tests
  (are [x y] (= (value p/template x) {:template y})
       simple-template          {0 "hello"}
       simple-template-spaces   {0 "es-verb form of"}
       end-with-pipe            {0 "hello" 1 ""}
       multiple-unnamed-params  {0 "hello", 1 "world"}
       named-param              {"hello" "world"}
       named-with-whitespace    {"named" "it is true indeed"}
       named-params             {"this" "that", 
                                 "named" "this is true",
                                 "please" "work"} 
       mixed-params             {0 "hello.!?$#!", 1 "world",
                                 "named" "true",    2 "what's up"}))


; Spanish adjustable  Adjective # [[#English|adjustable]], [[regulable]]
(deftest definition-test
  (testing "plain text definitions"
    (is (= [{:word "hello"} {:word "2"} {:word "world"}]
           (value p/definition "hello 2 world"))))
  (testing "link definitions"
    (testing "plain links"
      (is (= [{:link {:text "a link"}}]
             (value p/definition "[[a link]]"))))
    (testing "language specific links"
      (are [x y] (= (value p/definition x) y) 
           "[[#English|whatever]]"     [{:link {:text "whatever" :target "#English"}}]
           "[[acción#Spanish|Acción]]" [{:link {:text "Acción" :target "acción#Spanish"}}]
           "[[acción#Spanish]]"        [{:link {:text "acción#Spanish"}}])))
  (testing "mixed link and text definitions"
    (is (= [{:word "mixed"}
            {:link {:text "link"}}
            {:word "text"}
            {:link {:target "#Lang" :text "link"}}]
           (value p/definition "mixed [[link]] text [[#Lang|link]]"))))
  (testing "definitions has words surrounded by single quotes"
    (are [x y] (= [{:word x}] (value p/definition y))
         "lol"   "lol"
         "lol"   "''lol''"
         "lol"   "''''lol''''"
         "'lol'" "'lol'")))


;; TODO: these are nice for runnign and manually inspecting the value output,
;; but they should probably be real tests
(deftest full-entry-test
  (testing "Full word entries"
    (value p/entry "Spanish	corran	Verb	# {{uds.}} {{es-verb form of|formal=yes|person=second-person|number=plural|sense=affirmative|mood=imperative|ending=er|correr}}")
    (value p/entry "Spanish	cuanto	Pronoun	# {{context|in “[[en]] cuanto [[a]]...”}} however much concern; “[[regard]]”; [[regarding]]; [[as for]]")
    (value p/entry "Spanish	-aba	Suffix	# Suffix indicating the [[first-person singular]] [[imperfect]] [[indicative]] of [[-ar]] verbs.")
    (value p/entry "Spanish	&c.	{{abbreviation|es}}	# dongs {{obsolete form of|etc.|lang=es}} lol")
    (value p/entry  "Spanish	&c.	{{abbreviation|es}}	# dongs {{obsolete form of|etc.|lang=es}}, lol, [[wtf]], [butts]: please: why don't you work; ? huh?")))

;; Just here for convient sending to repl w/ cpp
(comment (run-tests))
