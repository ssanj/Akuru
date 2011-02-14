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
import MongoTypes.MongoToDomain

trait MongoObjectTrait extends Tools {

  case class MongoObject(dbo:DBObject) {

    import scala.collection.generic.CanBuildFrom

    def this() = this(new BasicDBObject)

    def this(tuples:Seq[Tuple2[String, Any]]) = this(new BasicDBObject(scala.collection.JavaConversions.asJavaMap(tuples.toMap)))

    def getPrimitive[T](key:String)(implicit con:AnyRefConverter[T]): T = getPrimitiveOfAnyType(key)

    def getPrimitive[T](f:Field[T])(implicit con:AnyRefConverter[T]): T = getPrimitiveOfAnyType(f.name)

    //TODO: Test
    def getMongo[T <: DomainObject : MongoToDomain](f:Field[T]): T = implicitly[MongoToDomain[T]].apply(dbo.get(f.name).asInstanceOf[DBObject])

    def getPrimitiveArray[T](key:String)(implicit con:AnyRefConverter[T]): Seq[T] =  getPrimitiveArrayOfAnyType[T](key)

    def getPrimitiveArray[T](f:Field[Seq[T]])(implicit con:AnyRefConverter[T]): Seq[T] = getPrimitiveArrayOfAnyType[T](f.name)

    def getId: MongoObjectId = MongoObjectId(dbo.get("_id").asInstanceOf[ObjectId])

    def getMongoArray[T <: DomainObject : MongoToDomain](key:String): Seq[T] = getAnyMongoArray[T](key)

    def getMongoArray[T <: DomainObject : MongoToDomain](f:Field[T]): Seq[T] = getAnyMongoArray[T](f.name)

    def putPrimitive[T](key:String, value:T): MongoObject = { putPrimitiveOfAnyType[T](key, value) }

    def putPrimitive[T](fv:FieldValue[T]): MongoObject = { putPrimitiveOfAnyType[T](fv.name, fv.value) }

    def putMongo(key:String, mongo:MongoObject): MongoObject = putAnyMongo(key, mongo)

    def putMongo[T <: DomainObject <% MongoObject](fv:FieldValue[T]): MongoObject = { putAnyMongo(fv.name, fv.value) }

    //todo: a DomainObject should already have a FieldValue[MongoObjectId]
    def putId(id:MongoObjectId): MongoObject = { dbo.put("_id", id.toObjectId); copyMongoObject }

    def merge(mo:MongoObject): MongoObject = { dbo.putAll(mo); copyMongoObject }

    def putMongoArray(key:String, values:Seq[MongoObject]): MongoObject = putAnyMongoArray(key, values)

    def putMongoArray[T <: DomainObject <% MongoObject](fv:FieldValue[Seq[T]]): MongoObject = putAnyMongoArray(fv.name,
      fv.value.map(implicitly[MongoObject](_)))

    //TODO: Test
    def putPrimitiveArray[T](key:String, values:Seq[T]): MongoObject = putPrimitiveArrayOfAnyType[T](key, values)

    //TODO: Test
    def putPrimitiveArray[T](fv:FieldValue[Seq[T]]): MongoObject = putPrimitiveArrayOfAnyType[T](fv.name, fv.value)

    private[akuru] def getPrimitiveOfAnyType[T](f: => String)(implicit con:AnyRefConverter[T]): T = con.convert(dbo.get(f))

    private[akuru] def getPrimitiveArrayOfAnyType[T](f: => String)(implicit con:AnyRefConverter[T]): Seq[T] = {
      import scala.collection.JavaConversions._
      val buffer = new ListBuffer[T]
      for(element <- dbo.get(f).asInstanceOf[BasicDBList].iterator) {
        buffer += (con.convert(element))
      }

      buffer.toSeq
    }

    private[akuru] def getAnyMongoArray[T <: DomainObject : MongoToDomain](key: => String): Seq[T] = {
      import scala.collection.JavaConversions._
      import MongoObject._
      val buffer = new ListBuffer[T]
      for(element <- dbo.get(key).asInstanceOf[BasicDBList].iterator) {
        buffer += (implicitly[MongoToDomain[T]].apply(element.asInstanceOf[DBObject]))
      }

      buffer.toSeq
    }

    private[akuru] def putPrimitiveOfAnyType[T](key: => String, value: => T): MongoObject = { dbo.put(key, value.asInstanceOf[AnyRef]); copyMongoObject }

    private[akuru] def putAnyMongo(key: => String, mongo: => MongoObject): MongoObject = {
      dbo.put(key, mongo.toDBObject.asInstanceOf[AnyRef]); copyMongoObject
    }

    private[akuru] def putPrimitiveArrayOfAnyType[T](key: => String, values: => Seq[T]): MongoObject = {
      import scala.collection.JavaConversions._
      val list:java.util.List[T] = values.toList
      dbo.put(key, list)
      copyMongoObject
    }

    private[akuru] def putAnyMongoArray[T](key: => String, values: => Seq[MongoObject]): MongoObject = {
      import scala.collection.JavaConversions._
      val list:java.util.List[DBObject] = values.map(_.toDBObject).toList
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
