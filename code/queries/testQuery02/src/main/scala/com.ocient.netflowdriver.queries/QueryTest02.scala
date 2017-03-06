package com.ocient.netflowdriver.queries

import com.beust.jcommander.JCommander
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import com.datastax.spark.connector._


object QueryTest02 {
  val config = new NetFlowArgs
  val keySpace = "netflow"
  val connectionsTableName = "connections"
  val netflowTableName = "netflow"



  def main(args: Array[String]): Unit = {

    new JCommander(this.config, args: _*)

    val sc = new SparkContext(new SparkConf())
    val local_ip = this.config.local_ip
    val local_port = this.config.local_port
    val remote_ip = this.config.remote_ip

    def doFilter(x:Connections): Boolean = {
      if ((local_ip >0) && (remote_ip>0) && (local_port == 0)) {
        return (x.local_ip == local_ip) && (x.remote_ip == remote_ip)
      }
      else if ((local_ip >0) && (remote_ip == 0) && (local_port > 0)) {
        return x.local_ip == local_ip && x.local_port == local_port
      }
      else if ((local_ip >0) && (remote_ip == 0) && (local_port == 0)) {
        return x.local_ip == local_ip
      }
      else if ((local_ip == 0) && (remote_ip > 0) && (local_port > 0)) {
        return x.remote_ip == remote_ip && x.local_port == local_port
      }
      else if ((local_ip == 0) && (remote_ip > 0) && (local_port == 0)) {
        return x.remote_ip == remote_ip
      }
      else if ((local_ip == 0) && (remote_ip == 0) && (local_port > 0)) {
        return x.local_port == local_port
      }
      true  // return everything
    }

    val connectionsTable = sc.cassandraTable[Connections](keySpace, connectionsTableName).cache
    val netflowTable = sc.cassandraTable[Netflow](keySpace, netflowTableName).cache

    // Filter the RDD down. Now we have only the connection IDs we want.
    //val connectionIDs = connectionsTable.filter(x => {x.local_ip == local_ip && x.remote_ip == remote_ip})
    val connectionIDs = connectionsTable.filter(doFilter)

    // Set up the keys
    val connectionsKeyed = connectionIDs.keyBy(theKey => theKey.connection_id)
    val netflowKeyed = netflowTable.keyBy(theKey => theKey.connection_id)

    // Join the filtered connection table to the netflow table.
    val results = connectionsKeyed.join(netflowKeyed).cache

    // De-normalized table
    val filteredNetflow = results.map(f => (new ConnectionsNetFlow(f._2._1.local_ip,f._2._1.remote_ip,
                                                f._2._1.local_port, f._2._2.start_time, f._2._2.dir_reason,
                                                f._2._2.end_time, f._2._2.num_bytes, f._2._2.num_packets,
                                                f._2._2.protocol ))).cache

    // Sum and group by
    val total = filteredNetflow.map(x => {
      if ((local_ip >0) && (remote_ip>0) && (local_port == 0)) {
        ((x.local_ip, x.remote_ip), x.num_bytes)
      }
      else if ((local_ip >0) && (remote_ip == 0) && (local_port > 0)) {
        ((x.local_ip, x.local_port), x.num_bytes)
      }
      else if ((local_ip >0) && (remote_ip == 0) && (local_port == 0)) {
        ((x.local_ip), x.num_bytes)
      }
      else if ((local_ip == 0) && (remote_ip > 0) && (local_port > 0)) {
        ((x.remote_ip, x.local_port), x.num_bytes)
      }
      else if ((local_ip == 0) && (remote_ip > 0) && (local_port == 0)) {
        ((x.remote_ip), x.num_bytes)
      }
      else if ((local_ip == 0) && (remote_ip == 0) && (local_port > 0)) {
        ((x.local_port), x.num_bytes)
      }
      else {
        ((x.local_ip, x.local_port, x.remote_ip), x.num_bytes)
      }
    }).reduceByKey((x,y)=>x+y)

    for (x<- total.collect()) {
      println(x._1 + ":" + x._2)
    }
  }
}
case class Netflow (connection_id: Long, start_time: Long, dir_reason: Int, end_time: Long, num_bytes:Int, num_packets: Int, protocol: Int);
case class Connections (local_ip: Long, remote_ip: Long, local_port: Int, connection_id: Long);
case class ConnectionsNetFlow (local_ip: Long, remote_ip: Long, local_port: Int, start_time: Long, dir_reason: Int, end_time: Long, num_bytes:Int, num_packets: Int, protocol: Int);