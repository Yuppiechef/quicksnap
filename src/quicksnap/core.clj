(ns quicksnap.core)

(defn flow [& {:as flow-setup}]
  flow-setup)

(defn state-fns [& {:as map-setup}]
  map-setup)

(defn run-machine
  ([fns flow] (run-machine :start fns flow {}))
  ([fns flow session] (run-machine :start fns flow session))
  ([state fns flow session & _]
     (if-let [next-flow (flow state)]
       (let [next-fn (fns (first next-flow))
             next-states (apply hash-map (rest next-flow))
             fnmap (apply hash-map
                          (flatten
                           (map #(vector (first %) (partial #'run-machine (second %) fns flow)) next-states)))]
         (next-fn state fnmap session)))))
