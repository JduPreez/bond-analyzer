(ns bond-analyzer.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [bond-analyzer.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[bond-analyzer started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[bond-analyzer has shut down successfully]=-"))
   :middleware wrap-dev})
