(ns munchcal-reagent.navbar
  (:require [munchcal-reagent.models.account :as account]))

(defn log-in-out []
  (if (account/logged-in?)
    [:a {:href "#/logout"} "Log Out"]
    [:a {:href "#/login"} "Log In"]))

(defn render []
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
      [:li [:a {:href "#/planner"} "Planner"]]
      [:li [:a {:href "#/about"} "About"]]]
     [:p {:class "navbar-text navbar-right"} (log-in-out)]]]])

