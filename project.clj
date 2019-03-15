(defproject checking-account "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring/ring-json "0.4.0"]
                 [clj-time "0.15.0"]
                 [cheshire "5.8.1"]
                 [ring/ring-mock "0.3.2"]]
  :main ^:skip-aot checking-account.core
  :target-path "target/%s"
  :source-paths ["src"]
  :ring {:handler checking-account.handler/app}
  :repl-options {:port 8081}
  :plugins [[lein-ring "0.12.5"]]
  :profiles {:uberjar {:aot :all}})
