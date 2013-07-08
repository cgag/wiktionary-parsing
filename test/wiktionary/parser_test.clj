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
  (let [{:keys [lang word pos]} (value p/basic-info test-entry)]
    (is (= "spanish" lang))
    (is (= "corran"  word))
    (is (= "verb"    pos))))

(deftest template-tests
  (let [simple        (value p/template simple-template)
        simple-spaces (value p/template simple-template-spaces)
        end-pipe      (value p/template end-with-pipe)
        mult-unamed   (value p/template multiple-unnamed-params)
        named-param   (value p/template named-param)
        named-whitespace (value p/template named-with-whitespace)
        named-params (value p/template named-params)
        mixed-params (value p/template mixed-params)]
    (is (= simple {:template {"0" "hello"}}))
    (is (= simple-spaces {:template {"0" "es-verb form of"}}))
    (is (= end-pipe {:template {"0" "hello" "1" ""}}))
    (is (= mult-unamed {:template {"0" "hello", "1" "world"}}))
    (is (= named-param {:template {"hello" "world"}}))
    (is (= named-whitespace {:template {"named" "it is true indeed"}}))
    (is (= named-params {:template {"this" "that", "named" "this is true",
                         "please" "work"}}))
    (is (= mixed-params {:template {"0" "hello.!?$#!", "1" "world",
                         "named" "true",    "2" "what's up"}}))))


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
      (is (= [{:link {:text "whatever" :language "english"}}] 
             (value p/definition "[[#English|whatever]]")))))
  (testing "mixed link and text definitions"
    (is (= [{:word "mixed"}
            {:link {:text "link"}}
            {:word "text"}
            {:link {:language "lang" :text "link"}}]
           (value p/definition "mixed [[link]] text [[#Lang|link]]")))))

(deftest full-entry-test
  (testing "Full word entries"
    (value p/entry "Spanish	corran	Verb	# {{uds.}} {{es-verb form of|formal=yes|person=second-person|number=plural|sense=affirmative|mood=imperative|ending=er|correr}}")))

(value p/entry "Spanish	correr	Verb	# to [[run]].")

;; Just here for convient sending to repl w/ cpp
(comment (run-tests))
