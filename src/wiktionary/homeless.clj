(ns wiktionary.homeless
  (:require [clojure.core.reducers :as r]))

(set! *warn-on-reflection* true)

(defn foldv [coll]
  (r/fold (r/monoid into vector) conj coll))
