# quicksnap

Quicksnap is a simple clojure state machine. The idea is that you can define a path for each state to travel along as a flow definition, then define which functions contain the logic for each of the states to prgress it to the next state. 

This idea splits up the notion of the flow graph from the functions that will be involved in actually performing the work.

## Usage

A simple robot that goes from initial (off?) state, then spins from red to green until it has changed 10 times can be written as follows :

First, we can define our flow as

```clojure
(def robot-flow
  (flow
   :start ["initialize" :next :red]
   :red ["next" :next :orange]
   :orange ["next" :next :green]
   :green ["next" :next :red]))
```

This flow will start at :start as you can imagine, then will bind the :next step to :red and call the function bound to "initialize" (currently unknown). :start will then proceed to :red, :red to :orange, :orange to :green and :green to :red, ad infinitum.

Let's demonstrate setting up our robot

```clojure
(defn init-robot [statename {next-fn :next} session]
  (println "Initialize Robot")
  (next-fn (assoc session :state "Started" :cnt 0)))
```

Not much to it, we accept the current state name, a map of functions to call in order to move forward and a session map that we can modify and pass on to the next function. We'll use the session to keep track of our last state and the change count.

Next up, we need a function for changing the light:

```clojure
(defn change-light [statename {next-fn :next} {cnt :cnt state :state :as session}]
  (println "Next" statename ", from state:" state)
  (if (< cnt 10)
    (next-fn (assoc session :state statename :cnt (inc cnt)))))
```

Here, again, we accept current state, the next functions and destructure our session to use it in our logic.

Now we've got all the pieces.. except the mapping for the flow functions of "initialize" and "next". Straightforward:

```clojure
(def robot-fns
  (state-fns
   "initialize" #'init-robot
   "next" #'change-light))
```

A fairly simple binding in this case. The reason for this extra binding step is that it is useful in case your function calls are anonymous or partial kung-fu calls. Mixing that directly into the flow detracts from the flow definition's purpose. 

Now we run the machine with a call to
```clojure
(run-machine robot-fns robot-flow)
```

And we get our result:
```
Initialize Robot
Next :red , old state: Started
Next :orange , old state: :red
Next :green , old state: :orange
Next :red , old state: :green
Next :orange , old state: :red
Next :green , old state: :orange
Next :red , old state: :green
Next :orange , old state: :red
Next :green , old state: :orange
Next :red , old state: :green
Next :orange , old state: :red
```

I'll leave making it switch state from red -> orange -> green -> red as an exercise to you :)

Notice that you can have multiple outcomes for each state :

```clojure
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
```

Check the quicksnap/sample.clj for the rest of the implementation for this.

*Please note:* This example blows the stack. I'm trying to decide how to handle the stack gracefully, so any input is welcome! My first thought was to use trampoline, but I'm not enitrely convinced this is how it should be done...

## License

Copyright (C) 2012 Yuppiechef Online (pty) ltd.

Distributed under the Eclipse Public License, the same as Clojure.
