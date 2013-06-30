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
  (let [t-value (partial value p/template)
        simple (t-value simple-template)
        simple-spaces (t-value simple-template-spaces)
        end-pipe (t-value end-with-pipe)
        mult-unamed (t-value multiple-unnamed-params)
        named-param (t-value named-param)
        named-whitespace (t-value named-with-whitespace)
        named-params (t-value named-params)
        mixed-params (t-value mixed-params)]
    (is (= simple {"0" "hello"}))
    (is (= simple-spaces {"0" "es-verb form of"}))
    (is (= end-pipe {"0" "hello" "1" ""}))
    (is (= mult-unamed {"0" "hello", "1" "world"}))
    (is (= named-param {"hello" "world"}))
    (is (= named-whitespace {"named" "it is true indeed"}))
    (is (= named-params {"this" "that", "named" "this is true",
                         "please" "work"}))
    (is (= mixed-params {"0" "hello.!?$#!", "1" "world",
                         "named" "true",    "2" "what's up"}))))


; Spanish adjustable  Adjective # [[#English|adjustable]], [[regulable]]
(deftest definition-test
  (let [parsed (value p/definition "hello [[test]] world")]
    (is (= parsed "hello test world"))))
