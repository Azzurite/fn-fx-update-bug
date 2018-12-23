(ns fn-fx-update-bug.core
  (:require [fn-fx.fx-dom :as dom]
            [fn-fx.controls :as ui]
            [fn-fx.diff :refer [component defui render should-update?]]))


(defui SettingsPane
  (render [this {:keys [prefs errors]}]
    (ui/grid-pane
      :children [(ui/label
                   :text "Settings content!")])))


(defui MainPane
  (render [this args]
    (ui/grid-pane
      :children [(ui/label
                   :text "MainPane content!")])))



(defui AppContainer
  (render [this {:keys [message] :as args}]
    (if (:in-settings? args)
      (ui/stack-pane
        :children [(ui/border-pane
                     :top (ui/h-box
                            :children [(ui/text
                                         :text (if (:in-settings? args)
                                                 "Settings"
                                                 "Welcome")
                                         :font (ui/font
                                                   :size 20))
                                       (ui/stack-pane
                                         :h-box/hgrow :always
                                         :alignment :center-right
                                         :children [(ui/hyperlink
                                                      :text (if (:in-settings? args) "Leave Settings" "Goto Settings")
                                                      :on-action {:event :toggle-settings})])])
                     :center (if (:in-settings? args)
                               (settings-pane args)
                               (main-pane args)))])
      (ui/border-pane
        :top (ui/h-box
               :children [(ui/text
                            :text (if (:in-settings? args)
                                    "Settings"
                                    "Welcome")
                            :font (ui/font
                                    :size 20))
                          (ui/stack-pane
                            :h-box/hgrow :always
                            :alignment :center-right
                            :children [(ui/hyperlink
                                         :text (if (:in-settings? args) "Leave Settings" "Goto Settings")
                                         :on-action {:event :toggle-settings})])])
        :center (if (:in-settings? args)
                  (settings-pane args)
                  (main-pane args))))))

(defui MainStage
  (render [this args]
    (ui/stage
      :title "Bug test app"
      :width 450
      :height 260
      :shown true
      :scene (ui/scene
               :root (app-container args)))))


(def app (atom {:in-settings? false}))

(defn create-gui [app-state]
  (let [handler-fn (fn [{:keys [event] :as event-data}]
                     (println "UI Event" event event-data)
                     (when (= event :toggle-settings)
                       (swap! app update :in-settings? not)))

        ui-state (agent (dom/app (main-stage @app-state) handler-fn))]
    (add-watch app-state :ui (fn [_ _ _ _]
                               (send ui-state
                                 (fn [old-ui]
                                   (println "Redraw" (pr-str @app-state))
                                   (let [stage (main-stage @app-state)]
                                     (dom/update-app old-ui stage))))))
    ui-state))

(defn -main []
  (create-gui app))
