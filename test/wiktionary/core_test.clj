(ns wiktionary.core-test
  (:require [wiktionary.core :as w]
            [clojure.test :refer :all]))

(deftest form-of-test
  (is (= (w/form-of "correr")   #{}))
  (is (= (w/form-of "recuerdo") #{"recordar"}))
  (is (= (w/form-of "corre")    #{"correr"}))
  (is (= (w/form-of "fue")      #{"ir" "ser"})))

(comment (run-tests))
