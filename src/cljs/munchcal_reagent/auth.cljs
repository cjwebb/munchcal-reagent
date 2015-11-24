(ns munchcal-reagent.auth
    (:require [munchcal-reagent.models.account :as account]
              [reagent.core :as reagent :refer [atom]]
              [reagent-forms.core :refer [bind-fields]]
              [goog.net.cookies :as cks]
              [ajax.core :as ajax]
              [munchcal-reagent.navbar :as navbar]
              [secretary.core :as secretary]))

(def ^:private auth-token "munchcal-auth-token")

(defn set-auth-token [token]
  (.set goog.net.cookies auth-token token))

(defn get-auth-token []
  (.get goog.net.cookies auth-token))

(defn remove-auth-token []
  (.remove goog.net.cookies auth-token))

(defn row [label input]
  [:div.row
   [:div.col-md-2 [:label label]]
   [:div.col-md-10 input]])

(def form-template
  [:div
   (row "Email" [:input {:field :email :id :email}])
   (row "Password" [:input {:field :password :id :password}])])

; todo - real error handling
(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(def api-url "http://localhost:3000")

(defn set-hash! [value]
  (aset (.-location js/window) "hash" value))

(defn login-handler [data]
  (do
    (set-auth-token (get-in data [:token :id]))
    (account/set! data)
    (set-hash! "/about")))

(defn get-account-info [token]
  (ajax/GET (str api-url "/auth/tokens/" token)
            {:handler account/set!
             :error-handler error-handler
             :response-format :json
             :keywords? true}))

(defn check-auth-token []
  (let [token (get-auth-token)]
    (if-not (nil? token)
      (get-account-info token)
      (remove-auth-token))))

(defn handle-login-submit [data e]
  (do
    (.preventDefault e)
    (ajax/POST (str api-url "/auth/login")
               {:params data
                :handler login-handler 
                :error-handler error-handler
                :response-format :json
                :keywords? true})))

(defn logout []
  (do
    (remove-auth-token)
    (account/unset!)
    (set-hash! "/")))

(defn login-page []
  (let [doc (atom {})]
    (fn []
      [:div
       (navbar/render)
       [:div.container
        [:div.page-header
         [:h1 "Login"]
         [:p.lead "Enter your email and password"]
         [:form {:method "get"
                 :action "#/login"
                 :onSubmit (partial handle-login-submit @doc)}
          [bind-fields form-template doc]
          [:button {:class "btn btn-info btn-lg" :type "submit"} "Login"]]]]])))

