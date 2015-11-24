(ns munchcal-reagent.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent-forms.core :refer [bind-fields]]
              [reagent.session :as session]
              [secretary.core :as sec :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [ajax.core :as ajax]
              [munchcal-reagent.about :as about]
              [munchcal-reagent.auth :as auth]
              [munchcal-reagent.navbar :as navbar]
              [munchcal-reagent.planner :as planner])
    (:use [clojure.walk :only [keywordize-keys]])
    (:import goog.History))

; todo - move to utils
(defonce api-url
  (-> (.getElementById js/document "server-originated-data")
      (.getAttribute "api-url")
      (cljs.reader/read-string)))

;; -------------------------
;; State
(def placeholder-image "placeholder-448x256.png")

(defonce recipes-search-params
  (atom {:q nil :from nil}))

(defn set-value! [value]
  (swap! recipes-search-params assoc :q value))

(defonce recipes-results
  (atom []))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-recipes-results []
  (let [q (:q @recipes-search-params)]
    (when (not (clojure.string/blank? q))
      (ajax/GET (str api-url "/recipes?q=" q)
                {:handler #(reset! recipes-results
                                   (:data (keywordize-keys %)))
                 :error-handler error-handler}))))

;; -------------------------
;; Views
(defn meal-view [meal]
  [:div {:class "meal-view col-xs-12 col-md-4" :key (:id meal)}
   [:a {:href (get-in meal [:source :url])
        :target "_blank"}
   [:img {:class "img-responsive"
          :src (get-in meal [:image :url] placeholder-image)}]
   [:h4 (:name meal)]
   [:ul.list-unstyled
    (clojure.string/join ", " (map :name (:ingredients meal)))]]])

(defn render-meals
  "Render a list of meals, with 3 on each row"
  [meals-atom]
  (let [meals @meals-atom
        threes (partition 3 3 nil meals)]
    (map #(vec [:div.row
                 {:key (:id (first %))}
                 (map meal-view %)])
         threes)))

(defn handle-search-submit [e]
  (do
    (get-recipes-results)
    (.preventDefault e)))

(defn recipe-search-box []
  [:div.col-md-12
   [:div#custom-search-input
   [:form {:method "get"
           :action "#/recipes"
           :onSubmit handle-search-submit}
    [:div.form-group
     [:div {:class "input-group col-md-12"}
      [:input {:type "text"
               :class "form-control"
               :id "q"
               :name "q"
               :placeholder "Search for a recipe e.g. Sandwich"
               :on-change #(set-value! (-> % .-target .-value))}]
      [:span.input-group-btn
       [:button {:class "btn btn-info btn-lg"
                 :type "submit"}
        [:i {:class "glyphicon glyphicon-search"}]]]]]]]])

(defn recipes-page []
  [:div
   (navbar/render)
   [:div.container
    [:div.page-header
     [:h1 "Recipes"]
     [:p.lead "Search and Discover"]]
    [:div.row (recipe-search-box)]
    (render-meals recipes-results)]])

(defn about-page []
  [:div
   (navbar/render)
   [:div.container
    [:div.page-header
     [:h1 "About"]
     [:p.lead "Your Kitchen AI"]]
     (about/text)]])

;; -------------------------
;; Routes

; todo - move navbar back into here
(defn current-page []
  [:div [(session/get :current-page)]])

(sec/set-config! :prefix "#")

(sec/defroute "/" []
  (session/put! :current-page #'recipes-page))

(sec/defroute "/about" []
  (session/put! :current-page #'about-page))

(sec/defroute "/login" []
  (session/put! :current-page #'auth/login-page))

(sec/defroute "/logout" []
  (auth/logout))

(sec/defroute "/planner" []
  (session/put! :current-page #'planner/dashboard))

;; -------------------------
;; Initialize app
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (sec/dispatch! (.-token event))))
    (.setEnabled true)))

(defn initialize-touch []
  (.initializeTouchEvents js/React true))

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (initialize-touch)
  (auth/check-auth-token)
  (planner/fetch-recipes)
  (mount-root))

