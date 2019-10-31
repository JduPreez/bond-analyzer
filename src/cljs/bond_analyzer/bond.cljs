(ns bond-analyzer.bond
  (:require [ajax.core :as ajax :refer [GET PUT POST]]
            [bond-analyzer.utils :as utils]
            [oz.core :as oz]
            [reagent.core :as reagent :refer [atom]]))

(def empty-bond {:id            nil
                 :description   ""
                 :updated       nil
                 :principal     0
                 :deposit       0
                 :term_years    0
                 :interest_rate 0})

(def amortization$ (atom []))
(def bond$ (atom empty-bond))

(defn final-principal [principal deposit]
  (- principal deposit))

(defn repayment [principal deposit interest-rate years]
  (let [final-principal (final-principal principal deposit)
        rate            (/ interest-rate 100)]
    (if (or (= final-principal 0)
            (= interest-rate 0)
            (= years 0))
      0.0
      (->> (+ rate (/ rate (- (Math/pow (+ 1 rate) years) 1)))
        (* final-principal)))))

(defn amortize [{term-years    :term_years
                 interest-rate :interest_rate
                 principal     :principal
                 deposit       :deposit}]
  (let [final-principal (final-principal principal deposit)
        rate            (/ interest-rate 100)
        payment         (repayment principal deposit interest-rate term-years)
        amortization    (loop [year     1
                               balance  final-principal
                               payments []]
                          (let [interest-paid  (* rate balance)
                                principal-paid (- payment interest-paid)
                                new-balance    (- balance principal-paid)
                                new-payments   (into [] cat [payments
                                                             [{:year   year
                                                               :item   "interest"
                                                               :amount interest-paid}
                                                              {:year   year
                                                               :item   "principal"
                                                               :amount principal-paid}]])]
                            (if (>= year term-years)
                              new-payments
                              (recur (+ 1 year)
                                     new-balance
                                     new-payments))))]
    (reset! amortization$ amortization)))

(defn- handler
  [response]
  (js/console.log (str "ENTER -handler, response: " response))
  (reset! bond$ response)
  (amortize @bond$))

(defn get-bond [id]
  (if id
    (GET (str "/bonds/" id)
      {:handler handler})
    (do (reset! bond$ empty-bond)
        (reset! amortization$ []))))

(defn save-bond! [bond]
  (js/console.log (str "BOND!!! " bond))
  (if (:id bond)
    (PUT (str "/bonds/" (:id bond))
      {:headers       {"Accept"       "application/transit+json"
                       "x-csrf-token" (.-value (.getElementById js/document "token"))}
       :params        bond
       :handler       #(js/console.log (str "Update bond response" %))
       :error-handler #(js/console.log (str "Update bond error" %))})
    (POST "/bonds"
      {:headers       {"Accept"       "application/transit+json"
                       "x-csrf-token" (.-value (.getElementById js/document "token"))}
       :params        bond
       :handler       #(reset! bond$ %)
       :error-handler #(js/console.log (str "Update bond error" %))})))
                   
(defn- bond-detail []
  (fn []
    [:div.content
     [:div {:class "field is-horizontal"}
      [:div {:class "field-label is-normal"}
       [:label {:class "label"} "Description"]]
      [:div {:class "field-body"}
       [:div {:class "field"}
        [:div {:class "control"}
         [:input {:class     "input form-control"
                  :type      "text"
                  :name      "description"
                  :value     (:description @bond$)
                  :on-change #(swap! bond$ assoc :description (-> % .-target .-value))}]]]]]

     [:div {:class "field is-horizontal"}
      [:div {:class "field-label"}
       [:label {:class "label"} "Purchase Price"]]
      [:div {:class "field-body"}
       [:div {:class "field is-narrow"}
        [:div {:class "control"}
         [:input {:class     "input"
                  :type      "number"
                  :name      "principal"
                  :value     (:principal @bond$)
                  :on-change #(swap! bond$ assoc :principal (-> % .-target .-value))}]]]]]

     [:div {:class "field is-horizontal"}
      [:div {:class "field-label"}
       [:label {:class "label"} "Deposit"]]
      [:div {:class "field-body"}
       [:div {:class "field is-narrow"}
        [:div {:class "control"}
         [:input {:class     "input"
                  :type      "number"
                  :name      "deposit"
                  :value     (:deposit @bond$)
                  :on-change #(swap! bond$ assoc :deposit (-> % .-target .-value))
                  :step      0.01}]]]]]

     [:div {:class "field is-horizontal"}
      [:div {:class "field-label"}
       [:label {:class "label"} "Interest Rate"]]
      [:div {:class "field-body"}
       [:div {:class "field is-narrow"}
        [:div {:class "control"}
         [:input {:class     "input"
                  :type      "number"
                  :name      "interest_rate"
                  :value     (:interest_rate @bond$)
                  :on-change #(swap! bond$ assoc :interest_rate (-> % .-target .-value))
                  :step      0.01
                  :min       0.01
                  :max       100}]]]]]

     [:div {:class "field is-horizontal"}
      [:div {:class "field-label"}
       [:label {:class "label"} "Term Years"]]
      [:div {:class "field-body"}
       [:div {:class "field is-narrow"}
        [:div {:class "control"}
         [:input {:class     "input"
                  :type      "number"
                  :name      "term_years"
                  :value     (:term_years @bond$)
                  :on-change #(swap! bond$ assoc :term_years (-> % .-target .-value))
                  :step      1
                  :min       1
                  :max       100}]]]]]

     [:div {:class "field is-horizontal"}
      [:div {:class "field-label"}
       [:label {:class "label"} "Final Loan Amount"]]
      [:div {:class "field-body"}
       [:div {:class "field is-narrow"}
        [:div {:class "control"}
         [:label {:class "label"} (final-principal (:principal @bond$)
                                                   (:deposit @bond$))]]]]]

     [:div {:class "field is-horizontal"}
      [:div {:class "field-label"}
       [:label {:class "label"} "Repayment Amount"]]
      [:div {:class "field-body"}
       [:div {:class "field is-narrow"}
        [:div {:class "control"}
         [:label {:class "label"} (utils/round (repayment (:principal @bond$)
                                                          (:deposit @bond$)
                                                          (:interest_rate @bond$)
                                                          (:term_years @bond$)) 2)]]]]]

     [:div {:class "field is-horizontal"}
      [:div {:class "field-label"}]
      [:div {:class "field-body"}
       [:div {:class "field is-grouped"}
        [:div {:class "control"}
         [:button {:class    "button is-link is-success"
                   :on-click #(amortize @bond$)} "Calculate"]]
        [:div {:class "control"}
         [:button {:class    "button is-link is-primary"
                   :on-click #(save-bond! @bond$)} "Save"]]
        [:div {:class "control"}
         [:a {:class "button is-light"
              :href  "#"} "Cancel"]]]]]
     [oz/vega {:data     {:values @amortization$}
               :mark     "bar"
               :width    610
               :height   300
               :encoding {:x     {:field "year"
                                  :type  "ordinal"}
                          :y     {:aggregate "sum"
                                  :field     "amount"
                                  :type      "quantitative"}
                          :color {:field "item"
                                  :type  "nominal"}}}]]))

(defn view [id]
  (get-bond id)
  (bond-detail))