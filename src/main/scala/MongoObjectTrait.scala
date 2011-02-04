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

  case class MongoObject(dbo:DBObject) {

    def this() = this(new BasicDBObject)

    def this(tuples:Seq[Tuple2[String, Any]]) = this(new BasicDBObject(scala.collection.JavaConversions.asJavaMap(tuples.toMap)))

    def get[T](key:String)(implicit con:AnyRefConverter[T]): T = con.convert(dbo.get(key))
    def getPlainArray[T](key:String)(implicit con:AnyRefConverter[T]): Seq[T] = {
      import scala.collection.JavaConversions._
      val buffer = new ListBuffer[T]
      for(element <- dbo.get(key).asInstanceOf[BasicDBList].iterator) {
        buffer += (con.convert(element))
      }

      buffer.toSeq
    }

    def getId: MongoObjectId = MongoObjectId(dbo.get("_id").asInstanceOf[ObjectId])

    def getArray[T](key:String)(implicit con:MongoConverter[T]): Seq[T] = {
      import scala.collection.JavaConversions._
      val buffer = new ListBuffer[T]
      for(element <- dbo.get(key).asInstanceOf[BasicDBList].iterator) {
        buffer += (con.convert(element.asInstanceOf[DBObject]))
      }

      buffer.toSeq
    }

    def put(key:String, value:Any) { dbo.put(key, value.asInstanceOf[AnyRef]) }

    def putMongo(key:String, mongo:MongoObject) { dbo.put(key, mongo.toDBObject.asInstanceOf[AnyRef]) }

    def putId(id:MongoObjectId) { dbo.put("_id", id.toObjectId) }

    def putArray(key:String, values:Seq[MongoObject]) {
      import scala.collection.JavaConversions._
      val list:java.util.List[DBObject] = values.map(_.toDBObject)
      dbo.put(key, list)
    }

    def putArray2[T](key:String, values:Seq[T]) {
      import scala.collection.JavaConversions._
      val list:java.util.List[T] = values
      dbo.put(key, list)
    }

    def toDBObject: DBObject = dbo

  }

  object MongoObject {
    implicit def dbObjectToMongoObject(dbo:DBObject): MongoObject = MongoObject(dbo)

    implicit def MongoObjectToDBObject(mo:MongoObject): DBObject = mo.toDBObject

    implicit def tuple2ToMongoObject(tuple2:Tuple2[String, Any]): MongoObject = {
      val mo = new MongoObject
      mo.put(tuple2._1, tuple2._2)
      mo
    }

    def push(col:String, value:MongoObject): MongoObject =  $func("$push", col, value)

    def set(col:String, value:AnyRef): MongoObject =  $funcAny("$set", col, value)

    def set(col:String, value:MongoObject): MongoObject =  $func("$set", col, value)

    def pull(col:String, value:MongoObject): MongoObject =  $func("$pull", col, value)

    def $func(action:String, col:String, value:MongoObject): MongoObject = {
      val parent = new MongoObject
      val element = new MongoObject
      element.putMongo(col, value)
      parent.putMongo(action, element)
      parent
    }

    def $funcAny(action:String, col:String, value:AnyRef): MongoObject = {
      val parent = new MongoObject
      val element = new MongoObject
      element.put(col, value)
      parent.putMongo(action, element)
      parent
    }

    def empty = new MongoObject

    def query(tuples:Tuple2[String, Any]*) = new MongoObject(tuples.toSeq)

    def mongoObject(tuples:Tuple2[String, Any]*) = new MongoObject(tuples.toSeq)
  }
}
