(ns wiktionary.homeless
  (:require [clojure.core.reducers :as r]))

(defn fold-into-vec [coll]
  (r/fold (r/monoid into vector) conj coll))
