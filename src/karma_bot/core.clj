(ns karma-bot.core
  (:import (java.net Socket)
           (java.io PrintWriter InputStreamReader BufferedReader)
           (clojure))
  (:gen-class))

(def freenode {:name "irc.freenode.net" :port 6667})
(def user {:name "roc_karma" :nick "roc_karma"})
(def id-val (atom {}))

(declare conn-handler)
(declare handle-message)
(declare update-ids)

(defn connect [server]
  (let [socket (Socket. (:name server) (:port server))
        in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        out (PrintWriter. (.getOutputStream socket))
        conn (ref {:in in :out out})]
    (doto (Thread. #(conn-handler conn)) (.start))
    conn))

(defn update-val [id val]
  "Add or update id with val"
  (swap! id-val assoc id val))

(defn write [conn msg]
  (doto (:out @conn)
    (.println (str msg "\r"))
    (.flush)))

(defn privmsg [conn receiver msg]
  (let [fullmsg (str "PRIVMSG " receiver " :" msg)]
    (write conn fullmsg)
    (println fullmsg)))

(defn conn-handler [conn]
  (while (nil? (:exit @conn))
    (let [msg (.readLine (:in @conn))]
      (println msg)
      (handle-message conn msg))))

(defn handle-message [conn msg]
  (cond
   (let [[_ from priv chan cmd] (re-find #":(.*)!~.* (PRIVMSG) (.*) :(.*)" msg)
         ;_ (println (str "from: " from " " priv " " chan " " cmd " "))
         ]
     (if (not (nil? cmd))
       (def parsed-cmd {:from from :cmd (clojure.string/split cmd #"\s") :chan chan})
       (def parsed-cmd nil))
     parsed-cmd)
   (let [cmd (:cmd parsed-cmd)
         firstcmd (first cmd)
         id (second cmd)
         chan (:chan parsed-cmd)
         _ (println cmd)]
     (cond
      (= firstcmd "!karma")
      (if (not= (count cmd) 1)
        (do
          (if (nil? (@id-val id))
            (update-val id 0))
          (privmsg conn chan (str id ": " (@id-val id))))
        (privmsg conn chan (str "hi " (:from parsed-cmd) "! Try using !karma <name>")))
      (= firstcmd "!about")
      (privmsg conn chan (str "roc_karma was created by Andrew D. Wong"))
      (= firstcmd "!map")
      (privmsg conn chan (str @id-val))
      ;; Search for ++ or --  
      :else (update-ids cmd)))
 
   (re-find #"^ERROR :Closing Link:" msg)
   (dosync (alter conn merge {:exit true}))
   (re-find #"^PING" msg)
   (write conn (str "PONG " (re-find #":.*" msg)))))

(defn update-ids [msg]
  (doall
   (map
    (fn [word]
      (let
          [
           [_ id]    (re-find #"(.*?)[(?:\+\+)(--)](.*)" word)
           [_ _ plus]  (re-find #"(.*?)((?:\+\+)+)\+?(.*)" word)
           [_ _ minus] (re-find #"(.*?)((?:--)+)-?(.*)" word)
           val (/ (+ (* -1 (count minus)) (count plus)) 2)
           ;_ (println "val: " val)
           ]
        (if (and (not= 0 val) (not= id ""))
          (update-val id
                      (if (not (nil? (@id-val id)))
                        (+ val (@id-val id))
                        val)))))
    msg)))

(defn login [conn user]
  (write conn (str "NICK " (:nick user)))
  (write conn (str "USER " (:nick user) " 0 * : " (:name user))))


(defn -main [& args]
  (def irc (connect freenode))
  (login irc user)
  (write irc "JOIN ##rochack")
  (println "Joining ##rochack"))

