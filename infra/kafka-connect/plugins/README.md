Place extra Kafka Connect plugins in this folder.

For C1+A2, add JDBC Sink connector here so Debezium + JDBC can run in the same Connect worker.

Expected content example:
- kafka-connect-jdbc/...
- (optional) additional JDBC driver jars

This folder is mounted in the Connect container at:
- /kafka/connect/plugins
