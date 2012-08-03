(ns quicksnap.sample
  (:use [quicksnap.core]))

(defn init-robot [{next-fn :next} {statename :quicksnap.core/statename :as session}]
  (println "Initialize Robot")
  (next-fn (assoc session :state "Started" :cnt 0)))

(defn change-light [{next-fn :next done-fn :finished} {statename :quicksnap.core/statename cnt :cnt state :state :as session}]
  (println "Next" statename ", from state:" state)
  (let [next-session (assoc session :state statename :cnt (inc cnt))]
    (if (< cnt 10)
      (next-fn next-session)
      (done-fn next-session))))

(defn stop-robot [_ {statename :quicksnap.core/statename state :state}]
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

(start-machine :start robot-fns robot-flow :debug-fn #'println)