(ns munchcal-reagent.prod
  (:require [munchcal-reagent.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
