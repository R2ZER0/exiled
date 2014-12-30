(defproject exiled "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.webbitserver/webbit "0.4.3"]
                 [org.clojure/data.json "0.1.2"]
                 [org.clojure/algo.generic "0.1.2"]]
  :main ^:skip-aot exiled.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
