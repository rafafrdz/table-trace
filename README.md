# Table Trace

TableTrace is a lightweight HTTP API that extracts the table names involved in a given SQL query. It supports parsing
queries and returning a JSON list of table names. This is a **Scala** version of
the [Table Trace](https://github.com/rafafrdz/table-trace-rs) project written in **Rust**.

## Getting Started

Follow these steps to set up and run your Table Trace api:

### Cloning the Repository

First, clone this repository to your local machine:

```bash
git clone git@github.com:rafafrdz/table-trace.git
cd table-trace
```

## Prerequisites

- JDK 11+
- SBT 1.8+
- Scala 2.13

### Running the Table Trace api locally

You can run the Table Trace api directly from SBT. Open a terminal in the project directory and execute:

```bash 
sbt run-api
```

By default, the service runs on [`http://localhost:9876`](http://localhost:9876)

## API Usage

### Endpoint

**POST** `/analyze`

**Body**: JSON with the field query containing the SQL string.

**Response**: JSON array with the extracted tables.

### Examples

#### Succeeded Request

```bash
curl -X POST http://localhost:9876/analyze -H "Content-Type: application/json" -d '{
"query": "UPDATE wine w SET stock = stock - (SELECT SUM(quantity) FROM order WHERE date = CURRENT_DATE AND order.wine_name = w.name) WHERE w.name IN (SELECT order.wine_name FROM order WHERE date = CURRENT_DATE)"
}'
```

#### Succeeded Response

```json
{
  "tables": [
    "wine",
    "order"
  ]
} 
```

#### Failed Request

```bash
curl -X POST http://localhost:9876/analyze -H "Content-Type: application/json" -d '{
"query": "UPDATE wine w WHERE w.name IN (SELECT order.wine_name FROM order WHERE date = CURRENT_DATE)"
}'
```

#### Failed Response

```text
Error processing the query `UPDATE wine w WHERE w.name IN (SELECT order.wine_name FROM order WHERE date = CURRENT_DATE)`
```

### Building the project

Prepare your project for distribution or deployment by building it with SBT:

1. Navigate to the project directory:

```bash
cd table-trace
```

2. Run the assembly:

```
sbt clean api/assembly
```

This command creates a JAR file in the `api/target/scala-2.13/` directory. For more details on configuring *
*sbt-assembly**, refer to the [sbt-assembly](https://github.com/sbt/sbt-assembly) documentation.

### Running the Uber JAR

Once the JAR is built, you can run the API directly with:

```bash
java -jar api/target/scala-2.13/table-trace-api-0.0.1.jar
```

By default, the service runs on [`http://localhost:9876`](http://localhost:9876)

## License

This project is available under your choice of the Apache 2.0 or CC0 1.0 license. Choose the one that best suits your
needs:

- [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- [CC0 1.0 Universal (Public Domain Dedication)](https://creativecommons.org/publicdomain/zero/1.0/)

This template is provided "as-is" without any warranties. Modify and distribute as needed to fit your project
requirements.