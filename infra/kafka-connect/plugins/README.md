Place extra Kafka Connect plugins in this folder.

For the current `debezium/connect:3.4` setup, the JDBC sink used in A2
(`io.debezium.connector.jdbc.JdbcSinkConnector`) is already bundled.
So this directory is optional for `c2a2`.

Use this folder only when you need additional plugins/drivers
(for example, Confluent JDBC sink or custom connectors).

This folder is mounted in the Connect container at:
- /kafka/connect/plugins
