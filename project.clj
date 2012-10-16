(defproject karma_bot "0.1.0-SNAPSHOT"
  :description "IRC karma bot"
  :main karma-bot.core
  :aot [karma-bot.db]
  :url "http://github.com/andrewwong1221/karma_bot"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [hyperion/hyperion-sqlite "3.2.0"]])
