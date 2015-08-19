(ns munchcal-reagent.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as sec :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType])
    (:import goog.History))

;; -------------------------
;; State
(defonce user-data
  (atom
    {:name "Colin"
     :id "6ca2e9c0-f4ed-11e4-b443-353a40402a60"}))

(def placeholder-image "placeholder-448x256.png")

(def today-meals
  (atom
    [{:id "1"
      :name "Peppered Chicken"
      :image-url placeholder-image
      :ingredients ["Chicken" "Black Pepper" "Salt"]}
     {:id "2"
      :name "Mediterranean Veg"
      :image-url placeholder-image
      :ingredients ["Courgette" "Red Pepper" "Onion"]}]))

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
     [:p {:class "navbar-text navbar-right"} (str "Signed in as " (@user-data :name)) ]]]])

(defn nice-join [xs]
  (clojure.string/join ", " xs))

(defn meal-view [meal]
  [:div {:class "col-xs-12 col-md-4"}
   [:img {:class "img-responsive" :src (:image-url meal)}]
   [:h4 (:name meal)]
   [:ul.list-unstyled
    (nice-join (:ingredients meal))]])

(defn home-page []
  [:div
   (navbar)
   [:div.container
    [:div.page-header
     [:h1 "Today's Meals"]
     [:p.lead "Tues 18th Aug"]]
    (map #(vec [:div.row (map meal-view %)])
         (partition 3 3 nil @today-meals))]])

(defn calendar-page []
  [:div
   (navbar)
   [:div.container
    [:h2 "Calendar"]
    [:p "Displays food for relevant week"]]])

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
    [:a {:href "#/my/recipes"} "My Recipes"]]])

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
