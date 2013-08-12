(ns wiktionary.core-test
  (:require [wiktionary.core :as w]
            [clojure.test :refer :all]))

(deftest form-of-test
  (are [x y] (= x y)
       (w/form-of "correr")   #{}
       (w/form-of "recuerdo") #{"recordar"}
       (w/form-of "corre")    #{"correr"}
       (w/form-of "fue")      #{"ir" "ser"}) )

(comment (run-tests))
