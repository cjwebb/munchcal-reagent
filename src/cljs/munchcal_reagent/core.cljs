(ns munchcal-reagent.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as sec :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [ajax.core :as ajax]
              [munchcal-reagent.about :as about])
    (:use [clojure.walk :only [keywordize-keys]])
    (:import goog.History))

(defonce api-url
  (-> (.getElementById js/document "server-originated-data")
      (.getAttribute "api-url")
      (cljs.reader/read-string)))

;; -------------------------
;; State
(def placeholder-image "placeholder-448x256.png")

(defonce user-has-searched?
  (atom false))

(defonce recipes-search-params
  (atom {:q nil :from nil}))

(defn set-value! [value]
  (swap! recipes-search-params assoc :q value))

(defonce recipes-results
  (atom []))

(defonce recipe-suggestions
  (atom []))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-recipes-results []
  (let [q (:q @recipes-search-params)]
    (when (not (clojure.string/blank? q))
      (ajax/GET (str api-url "/recipes?q=" q)
                {:handler #(do (reset! user-has-searched? true)
                               (reset! recipes-results
                                       (:data (keywordize-keys %))))
                 :error-handler error-handler}))))

(defn get-recipe-suggestions []
  (ajax/GET (str api-url "/recipes/random?limit=3")
            {:handler #(reset! recipe-suggestions
                               (:data (keywordize-keys %)))
             :error-handler error-handler}))

(add-watch recipes-results
           :watch-change #(cond (nil? (:data %4)) (get-recipe-suggestions)))

;; -------------------------
;; Views
(defn navbar []
  [:nav {:class "navbar navbar-default navbar-fixed-top"}
   [:div.container
    [:div.navbar-header
     [:button {:type "button"
               :class "navbar-toggle collapsed"
               :data-toggle "collapse"
               :data-target "#navbar"
               :aria-expanded "false"
               :aria-controls "navbar"}
      [:span.sr-only "Toggle Navigation"]
      [:span.icon-bar]
      [:span.icon-bar]
      [:span.icon-bar]]
     [:a {:class "navbar-brand" :href "#/"} "MunchCal"]]
    [:div {:id "navbar" :class "collapse navbar-collapse"}
     [:ul {:class "nav navbar-nav"}
      [:li [:a {:href "#/about"} "About"]]]]]])

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
  [meals]
  (let [threes (partition 3 3 nil meals)]
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

(defn display-suggestions []
  (let [h1 (if @user-has-searched? "No Results" "Suggestions")
        lead (if @user-has-searched?
               "Try some randomised recipes instead..."
               "Randomised, just for you")]
    [:div.page-header {:class "suggestions" }
      [:h1 h1]
      [:p.lead lead]
      (render-meals @recipe-suggestions)]))

(defn display-recipes []
  (let [meals @recipes-results]
    (if (not (empty? meals))
      (render-meals meals)
      (display-suggestions))))
; have a 'more' button, to refresh them?

(defn recipes-page []
  [:div
   (navbar)
   [:div.container
    [:div.page-header
     [:h1 "Recipes"]
     [:p.lead "Search and Discover"]]
    [:div.row (recipe-search-box)]
    (display-recipes)]])

(defn about-page []
  [:div
   (navbar)
   [:div.container
    [:div.page-header
     [:h1 "About"]
     [:p.lead "Your Kitchen AI"]]
     (about/text)]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(sec/set-config! :prefix "#")

(sec/defroute "/" []
  (session/put! :current-page #'recipes-page))

(sec/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (sec/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn initialize-touch []
  (.initializeTouchEvents js/React true))

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (initialize-touch)
  (mount-root)
  (get-recipe-suggestions))

