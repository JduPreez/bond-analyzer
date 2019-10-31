(ns bond-analyzer.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[bond-analyzer started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[bond-analyzer has shut down successfully]=-"))
   :middleware identity})
