(ns munchcal-reagent.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as sec :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType])
    (:import goog.History))

;; -------------------------
;; Views

(defn navbar []
  [:nav {:class "navbar navbar-default navbar-fixed-top"}
   [:div {:class "container"}
    [:div {:class "navbar-header"}
     [:a {:class "navbar-brand" :href "#/"} "MunchCal"]]
    [:div {:id "navbar" :class "collapse navbar-collapse"}
     [:ul {:class "nav navbar-nav"}
      [:li [:a {:href "#/calendar"} "Calendar"]]
      [:li [:a {:href "#/recipes"} "Recipes"]]]]]])

(defn home-page []
  [:div
   (navbar)
   [:div [:h2 "Welcome to Munchcal"]]])

(defn calendar-page []
  [:div
   (navbar)
    [:div [:h2 "Calendar"]]])

(defn recipes-page []
  [:div
   (navbar)
    [:div [:h2 "Recipes"]]])

(defn my-recipes-page []
  [:div
   (navbar)
    [:div [:h2 "My Recipes"]]])

(defn signup-page []
  [:div
   (navbar)
    [:div [:h2 "Sign Up"]]])

(defn login-page []
  [:div
   (navbar)
    [:div [:h2 "Login"]]])

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
