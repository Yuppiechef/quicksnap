(ns quicksnap.core)

(defn flow [& {:as flow-setup}]
  flow-setup)

(defn state-fns [& {:as map-setup}]
  map-setup)

(defn- combobulate
  [run-fn fns flow session state]
  (fn [& [ss & args]]
    (apply run-fn (second state) fns flow (or ss session) args)))

(defn- run-machine
  [state fns flow {debug-fn :debug-fn :as session} & args]
  (when debug-fn
    (debug-fn "Checking state" state fns flow session args))
  
  (if-let [next-flow (flow state)]
    (let [flowname (first next-flow)
          next-fn (if (string? flowname) (fns flowname) flowname)
          next-states (apply hash-map (rest next-flow))
          fnmap (into {}
                      (map
                       #(vector
                         (first %)
                         (combobulate #'run-machine fns flow session %))
                       next-states))]
      (when debug-fn
        (debug-fn "Starting state" state fns flow session args))
      (apply next-fn fnmap (assoc session ::statename state) (or args [])))))

(defn start-machine
  [start-state fns flow & {:as session}]
  (run-machine start-state fns flow session))