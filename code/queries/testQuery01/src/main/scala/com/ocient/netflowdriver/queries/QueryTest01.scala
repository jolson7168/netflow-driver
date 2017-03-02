package com.ocient.netflowdriver.queries

import com.beust.jcommander.JCommander
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import com.datastax.spark.connector._



object QueryTest01 {
  val config = new NetFlowArgs

  def main(args: Array[String]): Unit = {

    new JCommander(this.config, args: _*)

    val sc = new SparkContext(new SparkConf())
    val rdd = sc.cassandraTable(this.config.keyspace, this.config.table)
    println("Count: "+rdd.count)
    println("First: "+rdd.first)
  }
}
