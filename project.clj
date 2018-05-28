(defproject io.hosaka/registrar "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories ^:replace [["releases" "https://artifactory.i.hosaka.io/artifactory/libs-release"]
                           ["snapshots" "https://artifactory.i.hosaka.io/artifactory/libs-snapshot"]]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.cache "0.7.1"]
                 [org.clojure/tools.nrepl "0.2.13"]

                 [io.hosaka/common "1.0.0"]

                 [buddy/buddy-core "1.5.0.x"]

                 [com.rpl/specter "1.1.1"]
                 [metosin/scjsv "0.4.1"]

                 [org.apache.logging.log4j/log4j-core "2.11.0"]
                 [org.apache.logging.log4j/log4j-api "2.11.0"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.11.0"]

                 [yogthos/config "1.1.1"]

                 [ring/ring-core "1.6.2"]
                 [ring/ring-defaults "0.3.1"]

                 [org.postgresql/postgresql "42.2.2"]]
  :main ^:skip-aot io.hosaka.registrar
  :uberjar-name "registrar.jar"
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:resource-paths ["env/dev/resources" "resources"]
                   :env {:dev true}}})
