// Databricks notebook source
dbutils.widgets.text("cosmos-endpoint", "jpkadvisor")
dbutils.widgets.text("cosmos-database", "db")
dbutils.widgets.text("cosmos-collection", "coll2")

// COMMAND ----------

// Import Necessary Libraries
import com.microsoft.azure.cosmosdb.spark.schema._
import com.microsoft.azure.cosmosdb.spark._
import com.microsoft.azure.cosmosdb.spark.config.Config
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.types._
import org.apache.spark.sql.DataFrame
import spark.implicits._

// COMMAND ----------

def generateData() : DataFrame = {
  return Seq(
    (51.5, -1.1166667, 399.99, 3, "speaker"),
    (39.195, -94.68194, 1299.99, 5, "laptop"),
    (46.18806, -123.83, 399.99, 1, "speaker"),
    (-36.13333, 144.75, 89.99, 30, "headphones"),
    (33.52056, -86.8025, 149.99, 7, "monitor"),
    (39.79, -75.23806, 1299.99, 12, "laptop"),
    (40.69361, -89.58889, 399.99, 2, "speaker"),
    (36.34333, -88.85028, 149.9, 8, "monitor"),
    (48.883333, 2.15, 89.99, 2, "headphones"),
    (51.45, 5.466667, 159.99, 15, "monitor")
  ).toDF("lat", "lon", "price", "quantity", "item")
}

// COMMAND ----------

val endpoint = "https://" + dbutils.widgets.get("cosmos-endpoint") + ".documents.azure.com:443/"

// Write Configuration
val writeConfig = Config(Map(
  "Endpoint" -> endpoint,
  "Masterkey" -> dbutils.secrets.get(scope = "MAIN", key = "cosmos-key"),
  "Database" -> dbutils.widgets.get("cosmos-database"),
  "Collection" -> dbutils.widgets.get("cosmos-collection"),
  "Upsert" -> "true"
))

val data = generateData()

// Write data to Cosmos DB to set up the workflow, in a real scenario there would already be data in the account
data.write.mode(SaveMode.Overwrite).cosmosDB(writeConfig)

// COMMAND ----------

// Read Configuration
val readConfig = Config(Map(
  "Endpoint" -> endpoint,
  "Masterkey" -> dbutils.secrets.get(scope = "MAIN", key = "cosmos-key"),
  "Database" -> dbutils.widgets.get("cosmos-database"),
  "Collection" -> dbutils.widgets.get("cosmos-collection")
))

// Read the data back out of Cosmos DB
val records = spark.read.cosmosDB(readConfig)

// Save dataframe as a table
records.write.mode("overwrite").saveAsTable("cosmosData")