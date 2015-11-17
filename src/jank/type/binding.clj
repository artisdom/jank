(ns jank.type.binding
  (:require [jank.type.expression :as expression]
            [jank.type.declaration :as declaration])
  (:use clojure.pprint))

(defn lookup [name scope]
  "Recursively looks up a binding by name.
   Returns the binding, if found, or nil."
  (loop [current-scope scope]
    (when current-scope
      (if-let [found (find (:binding-definitions current-scope) name)]
        found
        (recur (:parent current-scope))))))

(defn add-to-scope [item scope]
  "Adds the binding to the scope and performs type checking on the
   initial value. Returns the updated scope."
  (let [item-name (second (second item))
        found (lookup item-name scope)
        item-type (expression/realize-type (nth item 2) scope)
        function (declaration/function? item-type)]
    (assert (or (nil? found)
                (and function
                     (= -1 (.indexOf found item-type))))
            (str "binding already exists: " item-name))

    (let [scope-with-decl (declaration/add-to-scope
                            [:declare-statement
                             [:identifier item-name]
                             [:type (into [:identifier] item-type)]]
                            scope)]
      (if (nil? found)
        (update
          scope-with-decl
          :binding-definitions assoc item-name [{:type item-type}])
        (update-in
          scope-with-decl
          [:binding-definitions item-name] conj {:type item-type})))))