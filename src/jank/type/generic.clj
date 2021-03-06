(ns jank.type.generic
  (:require [jank.type.expression :as expression]
            [clojure.walk :as walk])
  (:use jank.assert
        jank.debug.log))

(defn add-to-scope
  [item scope]
  (let [item-name (:name (:name item))]
    (update-in
      scope
      [:generic-lambas item-name] (fnil conj #{}) item)))

(defn map-type [map-acc [expected actual]]
  (if-let [existing (map-acc expected)]
    (do
      (type-assert (= existing expected)
                   (str "multiple substitutions for generic type " expected))
      map-acc)
    (assoc map-acc expected actual)))

(defn substitute [match type-map scope]
  ; TODO: Lookup definition in match scope
  ; TODO: Substitute actual types in, for return type too (by walking)
  nil)

(defn instantiate [call scope]
  (let [match-info (expression/overload-matches call scope)
        match (ffirst (:partial-matches match-info))]
    (pprint "call" call)
    (pprint "match" match)
    (pprint "generic-lambdas" (-> match :scope :generic-lambas))
    (if-not (contains? match :generics)
      call
      (let [generic-types (-> match :generics :values)
            ; TODO: Support explicit param specification
            expected-argument-types (-> match
                                        :value :generics
                                        :values first :values)
            actual-argument-types (:argument-types match-info)
            expected-actual-pairs (map vector
                                       expected-argument-types
                                       actual-argument-types)
            empty-type-mapping (zipmap generic-types (repeat nil))
            argument-type-mapping (reduce map-type
                                          empty-type-mapping
                                          expected-actual-pairs)]
        (type-assert (every? (comp some? second) argument-type-mapping)
                     (str "incomplete instantiation of "
                          (-> call :name :name)
                          " with type mapping "
                          argument-type-mapping))
        (let [instantiation (substitute match argument-type-mapping scope)]
          ; TODO: Type check instantiation before the assoc
          (assoc call
                 :instantiation instantiation))))))
