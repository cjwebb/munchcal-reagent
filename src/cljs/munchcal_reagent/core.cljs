(ns munchcal-reagent.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as sec :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [ajax.core :as ajax])
    (:import goog.History))

;; -------------------------
;; State
(defonce user-data
  (atom
    {:name "Colin"
     :id "6ca2e9c0-f4ed-11e4-b443-353a40402a60"}))

(def placeholder-image "placeholder-448x256.png")

(def todays-meals
  (atom
    [{:id "1"
      :name "Peppered Chicken"
      :ingredients [{:name "Chicken"} {:name "Black Pepper"} {:name "Salt"}]}
     {:id "2"
      :name "Mediterranean Veg"
      :ingredients [{:name "Courgette"} {:name "Red Pepper"} {:name "Onion"}]}]))

(defonce recipes-results
  (atom []))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-recipes-results []
  ; todo - pass in a search term
  ; todo - configurable endpoint?
  (ajax/GET "http://localhost:10020/recipes?q=carbonara"
            {:handler #(reset! recipes-results
                               (:data (clojure.walk/keywordize-keys %)))
             :error-handler error-handler}))

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
      [:li [:a {:href "#/calendar"} "Calendar"]]
      [:li [:a {:href "#/recipes"} "Recipes"]]]
     [:p {:class "navbar-text navbar-right"}
      (str "Signed in as " (@user-data :name))]]]])

(defn meal-view [meal]
  [:div {:class "col-xs-12 col-md-4" :key (:id meal)}
   [:img {:class "img-responsive"
          :src (get-in meal [:image :url] placeholder-image)}]
   [:h4 (:name meal)]
   [:ul.list-unstyled
    (clojure.string/join ", " (map :name (:ingredients meal)))]])

(defn render-meals
  "Render a list of meals, with 3 on each row"
  [meals]
  (map #(vec [:div.row {:key (:id (first %))} (map meal-view %)])
       (partition 3 3 nil meals)))

(defn home-page []
  [:div
   (navbar)
   [:div.container
    [:div.page-header
     [:h1 "Today's Meals"]
     [:p.lead "Tues 18th Aug"]]
    (render-meals @todays-meals)]])

(defn calendar-page []
  [:div
   (navbar)
   [:div.container
    [:h2 "Calendar"]
    [:p "Displays food for relevant week"]]])

(defn recipe-search-box []
  [:div.col-md-12
   [:form {:method "get"
           :action "#/recipes"
           :onSubmit (fn [e]
                      (.preventDefault e)
                      (get-recipes-results))}
    [:div.form-group
     [:input {:type "text"
              :class "form-control"
              :id "q"
              :name "q"
              :placeholder "Search for a recipe e.g. Spaghetti Carbonara"}]]]])

(defn recipes-page []
  [:div
   (navbar)
   [:div.container
    [:h2 "Recipes"]
    [:p "Recipe search"]
    [:p "If signed-in shows your favourites before you search, and search
        results including your favourites after searching"]
    [:p "If not signed-in, shows top recipes before you search, and
        normal results after searching" ]
    [:a {:href "#/my/recipes"} "My Recipes"]
    [:div.row (recipe-search-box)]
    (render-meals @recipes-results)]])

(defn my-recipes-page []
  [:div
   (navbar)
   [:div.container
    [:h2 "My Recipes"]
    [:p "List of my recipes, paginatable, with search"]]])

(defn signup-page []
  [:div
   (navbar)
   [:div.container [:h2 "Sign Up"]]])

(defn login-page []
  [:div
   (navbar)
   [:div.container [:h2 "Login"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(sec/set-config! :prefix "#")

(sec/defroute "/" []
  (session/put! :current-page #'home-page))

(sec/defroute "/calendar" []
  (session/put! :current-page #'calendar-page))

(sec/defroute "/recipes" []
  (session/put! :current-page #'recipes-page))

(sec/defroute "/my/recipes" []
  (session/put! :current-page #'my-recipes-page))

(sec/defroute "/signup" []
  (session/put! :current-page #'signup-page))

(sec/defroute "/login" []
  (session/put! :current-page #'login-page))

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
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
