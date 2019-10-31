(ns bond-analyzer.routes.home
  (:require [bond-analyzer.layout :as layout]
            [bond-analyzer.db.core :as db]
            [bond-analyzer.middleware :as middleware]
            [ring.util.http-response :as response]))

(defn home-page [request]
  (layout/render request "home.html"))

(defn get-bonds [_]
  (response/ok (db/get-bonds)))

(defn get-bond [{{:keys [id]} :path-params}]
  (response/ok (db/get-bond {:id id})))

(defn create-bond! [req]
  (let [bond (:params req)
        id   (:id (db/create-bond! bond))] 
    (response/ok (assoc bond :id id))))

(defn update-bond! [req]
  (let [bond (:params req)]
    (db/update-bond! bond)
    (response/ok bond)))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/bonds" {:get  get-bonds
              :post create-bond!}]
   ["/bonds/:id" {:get {:parameters {:path-params ::request}
                        :handler    get-bond}
                  :put update-bond!}]])