(ns munchcal-reagent.planner
    (:require [munchcal-reagent.models.account :as account]
              [reagent.core :as reagent :refer [atom]]
              [reagent-forms.core :refer [bind-fields]]
              [ajax.core :as ajax]
              [munchcal-reagent.navbar :as navbar]))

(defonce food2
  (atom []))

(def recipe-ids
  ["437748b2-f88f-4427-b600-7a06de05099d"
   "8912b15e-761d-4592-a057-38a73895046a"
   "adf0f056-9980-4fbe-9022-65a23248eddc"
   "14e848c6-8625-4972-a663-4760b652eb7f"
   "2def3fcc-c0ea-4106-b3f4-f1788ea41872"])

(def food
  [{:date "Monday 19th"
    :meal {:id "d0c93a60-e143-47aa-aced-398c82f3ad11"
           :name "Chorizo, chicken and chickpea casserole"
           :description "Simon Rimmer's Spanish style one-pot supper is full of flavour and quick to prepare."
           :image {:url "https://images.munchcal.com/recipes/d0c93a60-e143-47aa-aced-398c82f3ad11.jpg"}}}])

(defn render-day [data]
  [:div.row {:key (:id data)}
   [:div {:class "col-xs-12"}
    [:img {:class "img-responsive"
           :src (get-in data [:image :url])}]
    [:h4 (get-in data [:name])]]])

(defn dashboard []
  [:div
   (navbar/render)
   [:div.container
    [:div.page-header
     [:h1 "Planner"]
      [:p.lead "Ideas for this week"]]
    (map render-day @food2)]])

(def api-url "https://api.munchcal.com")

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn fetch-recipe [id]
  (do
    (ajax/GET (str api-url "/recipes/" id)
              {:handler #(swap! food2 concat (:data %)) 
               :error-handler error-handler
               :response-format :json
               :keywords? true})))

(defn fetch-recipes []
  (doall (map fetch-recipe recipe-ids)))

