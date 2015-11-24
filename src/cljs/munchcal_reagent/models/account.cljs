(ns munchcal-reagent.models.account
    (:require [reagent.core :as reagent :refer [atom]]))

;; -----------------------
;; Account Information
(defonce account
  (atom {}))

(defn set! [data]
  (reset! account data))

(defn unset! []
  (reset! account {}))

(defn get-token! []
  (get-in @account [:token :id]))

(defn logged-in? []
  (let [email (get-in @account [:account :email])
        name (get-in @account [:account :name])]
    (not
      (and (nil? (get-token!))
           (nil? email)
           (nil? name)))))

