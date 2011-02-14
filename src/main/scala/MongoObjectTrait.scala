/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import collection.mutable.ListBuffer
import com.mongodb.{BasicDBList, BasicDBObject, DBObject}
import org.bson.types.ObjectId
import MongoTypes.MongoObjectId
import MongoTypes.DomainObject
import MongoTypes.FieldValue
import MongoTypes.Field

trait MongoObjectTrait extends Tools {

  case class MongoObject(dbo:DBObject) {

    import scala.collection.generic.CanBuildFrom

    def this() = this(new BasicDBObject)

    def this(tuples:Seq[Tuple2[String, Any]]) = this(new BasicDBObject(scala.collection.JavaConversions.asJavaMap(tuples.toMap)))

    def getPrimitive[T](key:String)(implicit con:AnyRefConverter[T]): T = getPrimitiveOfAnyType(key)

    def getPrimitive[T](f:Field[T])(implicit con:AnyRefConverter[T]): T = getPrimitiveOfAnyType(f.name)

    def getPrimitiveArray[T](key:String)(implicit con:AnyRefConverter[T]): Seq[T] =  getPrimitiveArrayOfAnyType[T](key)

    def getPrimitiveArray[T](f:Field[T])(implicit con:AnyRefConverter[T]): Seq[T] =  getPrimitiveArrayOfAnyType[T](f.name)

    private[akuru] def getPrimitiveOfAnyType[T](f: => String)(implicit con:AnyRefConverter[T]): T = con.convert(dbo.get(f))

    private[akuru] def getPrimitiveArrayOfAnyType[T](f: => String)(implicit con:AnyRefConverter[T]): Seq[T] = {
      import scala.collection.JavaConversions._
      val buffer = new ListBuffer[T]
      for(element <- dbo.get(f).asInstanceOf[BasicDBList].iterator) {
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

    def putPrimitive[T](key:String, value:T): MongoObject = { putPrimitiveOfAnyType[T](key, value) }

    def putPrimitive[T](fv:FieldValue[T]): MongoObject = { putPrimitiveOfAnyType[T](fv.name, fv.value) }

    private[akuru] def putPrimitiveOfAnyType[T](key: => String, value: => T): MongoObject = { dbo.put(key, value.asInstanceOf[AnyRef]); copyMongoObject }

    def putMongo(key:String, mongo:MongoObject): MongoObject = putAnyMongo(key, mongo)

    def putMongo[T <: DomainObject <% MongoObject](fv:FieldValue[T]): MongoObject = { putAnyMongo(fv.name, fv.value) }

    private[akuru] def putAnyMongo(key: => String, mongo: => MongoObject): MongoObject = {
      dbo.put(key, mongo.toDBObject.asInstanceOf[AnyRef]); copyMongoObject
    }

    def putId(id:MongoObjectId): MongoObject = { dbo.put("_id", id.toObjectId); copyMongoObject }

    def merge(mo:MongoObject): MongoObject = { dbo.putAll(mo); copyMongoObject }

    def putMongoArray(key:String, values:Seq[MongoObject]): MongoObject = putAnyMongoArray(key, values)

    def putMongoArray[T <: DomainObject <% MongoObject](fv:FieldValue[Traversable[T]]): MongoObject = putAnyMongoArray(fv.name,
      fv.value.map(implicitly[MongoObject](_)))

    def putAnyMongoArray(key: => String, values: => Traversable[MongoObject]): MongoObject = {
      import scala.collection.JavaConversions._
      val list:java.util.List[DBObject] = values.map(_.toDBObject).toList
      dbo.put(key, list)
      copyMongoObject
    }

    //TODO: Test
    def putPrimitiveArray[T](key:String, values:Traversable[T]): MongoObject = putPrimitiveArrayOfAnyType[T](key, values)

    //TODO: Test
    def putPrimitiveArray[T](fv:FieldValue[Traversable[T]]): MongoObject = putPrimitiveArrayOfAnyType[T](fv.name, fv.value)

    private[akuru] def putPrimitiveArrayOfAnyType[T](key: => String, values: => Traversable[T]): MongoObject = {
      import scala.collection.JavaConversions._
      val list:java.util.List[T] = values.toList
      dbo.put(key, list)
      copyMongoObject
    }

    private[akuru] def toDBObject: DBObject = new BasicDBObject(dbo.toMap)

    private[akuru] def copyMongoObject: MongoObject = MongoObject(toDBObject)
  }

  object MongoObject {
    implicit def dbObjectToMongoObject(dbo:DBObject): MongoObject = MongoObject(dbo)

    implicit def MongoObjectToDBObject(mo:MongoObject): DBObject = mo.toDBObject

    implicit def tuple2PrimitiveToMongoObject(tuple2:Tuple2[String, AnyRef]): MongoObject =  mongo.putPrimitive(tuple2._1, tuple2._2)

    implicit def tuple2MongoToMongoObject(tuple2:Tuple2[String, MongoObject]): MongoObject =  mongo.putMongo(tuple2._1, tuple2._2)

    //TODO: remove this
    implicit def dboToDBOToMongo(dbo:DBObject): DBOToMongo = DBOToMongo(dbo)

    def push(col:String, value:MongoObject): MongoObject =  $funcMongo("$push", col, value)

    def set(col:String, value:AnyRef): MongoObject =  $funcPrimitive("$set", col, value)

    def set(col:String, value:MongoObject): MongoObject =  $funcMongo("$set", col, value)

    def pull(col:String, value:MongoObject): MongoObject =  $funcMongo("$pull", col, value)

    def pull(col:String, value:AnyRef): MongoObject =  $funcPrimitive("$pull", col, value)

    def $funcMongo(action:String, col:String, value:MongoObject): MongoObject =  mongo.putMongo(action, mongo.putMongo(col, value))

    def $funcPrimitive(action:String, col:String, value:AnyRef): MongoObject =  mongo.putMongo(action, mongo.putPrimitive(col, value))

    def empty = new MongoObject

    def mongo = new MongoObject

    def query(tuples:Tuple2[String, AnyRef]*) = new MongoObject(tuples.toSeq)

    def mongoObject(tuples:Tuple2[String, AnyRef]*) = new MongoObject(tuples.toSeq)

    //TODO: remove this
    case class DBOToMongo(dbo:DBObject) {
      def toMongo(): MongoObject = MongoObject(dbo)
    }
  }
}
