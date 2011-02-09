/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import collection.mutable.ListBuffer
import com.mongodb.{BasicDBList, BasicDBObject, DBObject}
import org.bson.types.ObjectId
import MongoTypes.MongoObjectId

trait MongoObjectTrait extends Tools {

  //TODO: Make this class immutable
  case class MongoObject(dbo:DBObject) {

    def this() = this(new BasicDBObject)

    def this(tuples:Seq[Tuple2[String, Any]]) = this(new BasicDBObject(scala.collection.JavaConversions.asJavaMap(tuples.toMap)))

    def getPrimitive[T](key:String)(implicit con:AnyRefConverter[T]): T = con.convert(dbo.get(key))

    def getPrimitiveArray[T](key:String)(implicit con:AnyRefConverter[T]): Seq[T] = {
      import scala.collection.JavaConversions._
      val buffer = new ListBuffer[T]
      for(element <- dbo.get(key).asInstanceOf[BasicDBList].iterator) {
        buffer += (con.convert(element))
      }

      buffer.toSeq
    }

    def getId: MongoObjectId = MongoObjectId(dbo.get("_id").asInstanceOf[ObjectId])

    def getMongoArray[T, R >: MongoObject <% T](key:String): Seq[T] = {
      import scala.collection.JavaConversions._
      import MongoObject._
      val buffer = new ListBuffer[T]
      for(element <- dbo.get(key).asInstanceOf[BasicDBList].iterator) {
        buffer += (element.asInstanceOf[DBObject].toMongo)
      }

      buffer.toSeq
    }

    def putPrimitive[T](key:String, value:T): MongoObject = { dbo.put(key, value.asInstanceOf[AnyRef]); MongoObject(dbo) }

    def putMongo(key:String, mongo:MongoObject): MongoObject = { dbo.put(key, mongo.toDBObject.asInstanceOf[AnyRef]); MongoObject(dbo) }

    def putId(id:MongoObjectId): MongoObject = { dbo.put("_id", id.toObjectId); MongoObject(dbo) }

    def merge(mo:MongoObject): MongoObject = { dbo.putAll(mo); MongoObject(dbo) }

    def putMongoArray(key:String, values:Seq[MongoObject]): MongoObject = {
      import scala.collection.JavaConversions._
      val list:java.util.List[DBObject] = values.map(_.toDBObject)
      dbo.put(key, list)
      MongoObject(dbo)
    }

    def putPrimitiveArray[T](key:String, values:Seq[T]): MongoObject = {
      import scala.collection.JavaConversions._
      val list:java.util.List[T] = values
      dbo.put(key, list)
      MongoObject(dbo)
    }

    //TODO:Make this return a copy
    def toDBObject: DBObject = dbo
  }

  object MongoObject {
    implicit def dbObjectToMongoObject(dbo:DBObject): MongoObject = MongoObject(dbo)

    implicit def MongoObjectToDBObject(mo:MongoObject): DBObject = mo.toDBObject

    implicit def tuple2PrimitiveToMongoObject(tuple2:Tuple2[String, Any]): MongoObject =  mongo.putPrimitive(tuple2._1, tuple2._2)

    implicit def tuple2MongoToMongoObject(tuple2:Tuple2[String, MongoObject]): MongoObject =  mongo.putMongo(tuple2._1, tuple2._2)

    def push(col:String, value:MongoObject): MongoObject =  $funcMongo("$push", col, value)

    def set(col:String, value:AnyRef): MongoObject =  $funcPrimitive("$set", col, value)

    def set(col:String, value:MongoObject): MongoObject =  $funcMongo("$set", col, value)

    def pull(col:String, value:MongoObject): MongoObject =  $funcMongo("$pull", col, value)

    def pull(col:String, value:AnyRef): MongoObject =  $funcPrimitive("$pull", col, value)

    def $funcMongo(action:String, col:String, value:MongoObject): MongoObject =  mongo.putMongo(action, mongo.putMongo(col, value))

    def $funcPrimitive(action:String, col:String, value:AnyRef): MongoObject =  mongo.putMongo(action, mongo.putPrimitive(col, value))

    def empty = new MongoObject

    def mongo = new MongoObject

    def query(tuples:Tuple2[String, Any]*) = new MongoObject(tuples.toSeq)

    def mongoObject(tuples:Tuple2[String, Any]*) = new MongoObject(tuples.toSeq)

    implicit def tToMongoObject(t:Tuple2[String, Any]*): MongoObject = new MongoObject(t.toSeq)

    implicit def dboToDBOToMongo(dbo:DBObject): DBOToMongo = DBOToMongo(dbo)

    case class DBOToMongo(dbo:DBObject) {
      def toMongo(): MongoObject = MongoObject(dbo)
    }
  }
}
