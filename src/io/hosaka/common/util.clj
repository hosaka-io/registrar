(ns io.hosaka.common.util)

(defn get-port [port]
  (cond
    (string? port) (try (Integer/parseInt port)
                        (catch Exception e nil))
    (integer? port) port
    :else nil))
