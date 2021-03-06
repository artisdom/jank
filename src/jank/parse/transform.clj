(ns jank.parse.transform
  (:require [jank.parse.fabricate :as fabricate])
  (:use jank.assert
        jank.debug.log))

; TODO: Use fabricate for all of these?

(defn single [kind value]
  {:kind kind :value value}) ; TODO: add single-body and single-values

(defn read-single [kind value]
  {:kind kind :value (read-string value)}) ; TODO: Use a safer option

(defn identifier [& more]
  (let [base {:kind :identifier
              :name (first more)}]
    (if (= 1 (count more))
      base
      (assoc base :generics (second more)))))

(defn specialization-list [& more]
  {:kind :specialization-list
   :values (or more [])})

(defn generic-specialization-list [& more]
  {:kind :generic-specialization-list
   :values (or more [])})

(defn declaration [kind & more]
  (let [base {:kind (if (= kind :type)
                      :type-declaration
                      :binding-declaration)
              :external? (= "declare-extern" (first more))
              :type (last more)}
        size (count more)]
    (parse-assert (or (= kind :type)
                      (= 3 size))
                  "invalid declaration")
    (if (not= kind :type) ; Has identifier (declaring a binding)
      (assoc base :name (second more))
      base)))

(defn binding-definition [& more]
  (let [base {:kind :binding-definition
              :name (first more)
              :value (last more)}
        size (count more)]
    (if (= 3 size) ; Has type
      (assoc base :type (second more))
      base)))

(defn struct-definition [& more]
  {:kind :struct-definition
   :name (first more)
   :members (into [] (rest more))
   :type {:kind :type
          :value (first more)}})

(defn struct-member [& more]
  (let [base {:kind :struct-member
              :name (first more)
              :type (second more)}
        size (count more)]
    (if (= 3 size) ; Has value
      (assoc base :value (nth more 2))
      base)))

(defn new-expression [& more]
  {:kind :new-expression
   :specialization-list (first more)
   :values (into [] (rest more))})

(defn function-call [& more]
  {:kind :macro-function-call
   :name (first more)
   :arguments (rest more)})

(defn macro-definition [& more]
  {:kind :macro-definition
   :name (first more)
   :arguments (second more)
   :body (into [] (drop 2 more))})

(defn keyword-to-type [kw]
  ; Remove the prefixed colon
  (fabricate/type (subs (:value kw) 1)))

(defn lambda-definition [& more]
  (let [generics (when (= :generic-specialization-list
                          (-> more first :kind))
                   (update (first more) :values (partial map keyword-to-type)))
        generic? (some? generics)
        more (if generic?
               (rest more)
               more)]
    {:kind :lambda-definition
     :generic? generic?
     :generics generics
     :arguments (first more)
     :return (second more)
     :body (into [] (drop 2 more))}))

(defn argument-list [& more]
  {:kind :argument-list
   :values (into [] more)})

(defn macro-argument-list [& more]
  {:kind :macro-argument-list
   :values (into [] more)})

(defn syntax-definition [& more]
  {:kind :syntax-definition
   :body (into [] more)})

(defn syntax-list [& more]
  {:kind :syntax-list
   :body (into [] more)})

(defn syntax-item [& more]
  {:kind :syntax-item
   :value (first more)})

(defn syntax-escaped-item [& more]
  {:kind :syntax-escaped-item
   :body (into [] more)})

(defn escaped-item [& more]
  {:kind :escaped-item
   :body (into [] more)})

(defn return-list [& more]
  {:kind :return-list
   :values (into [] more)})

(defn if-expression [& more]
  (let [base {:kind :if-expression
              :condition (first more)
              :then (second more)}]
    (if (= 2 (count more))
      base
      (assoc base :else (nth more 2)))))
