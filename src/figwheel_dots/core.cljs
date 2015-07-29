(ns ^:figwheel-always figwheel-dots.core
    (:require
     [cljs.core.async :refer [timeout chan >! <!]])
    (:require-macros
     [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {1 {:id 1 :x 100 :y 200 :val 100}
                          2 {:id 2 :x 300 :y 200 :val 0}}))
(defonce running (atom true))

(def canvas (-> js/document (.getElementById "canvas")))
(def context (.getContext canvas "2d"))
(def width (.-width canvas))
(def height (.-height canvas))
(def background "white")
(def colors ["red" "pink" "lightgray" "lightblue" "green" "lightgreen" "orange" "yellow"])
(def d 50)

(defn setText [context color style]
  (set! (.-fillStyle context) color)
  (set! (.-font context) style))

(defn setColor [context color]
  (set! (.-fillStyle context) color)
  (set! (.-globalAlpha context) 1.0))


(defn draw-dot [{:keys [x y val]}]
  (doto context
       (setColor "lightgreen")
       .beginPath
       (.arc  x y d 0 (* 2 Math/PI) true)
       .closePath
       .fill )
  (doto context
    (setText "black" "bold 11px Courier")
    (.fillText (str val) (- x 7) (+ y 5))))

(defn draw-dots [state]
  (doall (map draw-dot (vals @app-state))))

(defn dot-work [{:keys [id x y val] :as dot}]
  (update dot :val inc))

(defn work-dots [state]
  (let [new-dot-vals (doall (map dot-work (vals @app-state)))]
    (reset! app-state (zipmap (map :id new-dot-vals) new-dot-vals))))

(defn clear []
  (doto context
    (setColor background)
    (.fillRect  0 0 width height)))

(defn tick []
  (work-dots app-state)
  (clear)
  (draw-dots app-state))

(defn time-loop []
  (go
    (<! (timeout 60))
    (tick)
    (.requestAnimationFrame js/window time-loop)))

(defn run []
  (.requestAnimationFrame
   js/window
   (fn [_]
     (time-loop))))

(run)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
