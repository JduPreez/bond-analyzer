(ns bond-analyzer.bonds
  (:require [ajax.core :as ajax]
            [reagent.core :as reagent :refer [atom]]))

(def bonds$ (atom nil))

(defn- bonds-list []
  (fn []
    [:div.content
     [:div {:class "control"}
      [:a {:class "button is-primary"
           :href  "#/bonds/new"} "Add New Bond"]]
     [:table {:class "table is-striped"}
      [:thead
       [:tr
        [:th {:scope "col"} "Description"]
        [:th {:scope "col"} "Time"]
        [:th {:scope "col"} "Purchase Price"]
        [:th {:scope "col"} "Deposit"]
        [:th {:scope "col"} "Term Years"]
        [:th {:scope "col"} "Interest Rate"]]]
      [:tbody
       (map (fn [bond]
              (let [{id            :id
                     description   :description
                     updated       :updated
                     principal     :principal
                     deposit       :deposit
                     term_years    :term_years
                     interest_rate :interest_rate} bond]
                ^{:key (str "bond-item-" id)}
                [:tr
                 [:th [:a {:href (str "#/bonds/" id)} description]]
                 [:td updated]
                 [:td principal]
                 [:td deposit]
                 [:td term_years]
                 [:td interest_rate]]))
            @bonds$)]]]))

(defn- handler
  [response]
  (let []
    (reset! bonds$ response)))

(defn get-bonds []
  (ajax/GET "/bonds"
            {:handler handler}))

(defn view []
  (get-bonds)
  (bonds-list))