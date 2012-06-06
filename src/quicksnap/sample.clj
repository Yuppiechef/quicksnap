(ns quicksnap.sample
  (:use [quicksnap.core]))

(defn init-robot [statename {next-fn :next} session]
  (println "Initialize Robot")
  (next-fn (assoc session :state "Started" :cnt 0)))

(defn change-light [statename {next-fn :next done-fn :finished} {cnt :cnt state :state :as session}]
  (println "Next" statename ", from state:" state)
  (let [next-session (assoc session :state statename :cnt (inc cnt))]
    (if (< cnt 10)
      (next-fn next-session)
      (done-fn next-session))))

(defn stop-robot [statename _ {state :state}]
  (println "Finished at" state))

(def robot-fns
  (state-fns
   "initialize" #'init-robot
   "next" #'change-light
   "finished" #'stop-robot))

(def robot-flow
  (flow
   :start ["initialize" :next :red]
   :red ["next"
         :next :orange
         :finished :done]
   :orange ["next"
            :next :green
            :finished :done]
   :green ["next"
           :next :red
           :finished :done]
   :done ["finished"]))

(run-machine robot-fns robot-flow)