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


    def this() = this(new BasicDBObject)

    def this(tuples:Seq[Tuple2[String, Any]]) = this(new BasicDBObject(scala.collection.JavaConversions.asJavaMap(tuples.toMap)))

    def getPrimitive[T](key:String)(implicit con:AnyRefConverter[T]): T =
      getAnyArrayType[T](asSingleElementContainer(key))(element => con.convert(element)).head

    def getPrimitive[T](f:Field[T])(implicit con:AnyRefConverter[T]): T = getPrimitive[T](f.name)

    //TODO: Test
    def getMongo[T <: DomainObject : MongoToDomain](f:Field[T]): T = implicitly[MongoToDomain[T]].apply(dbo.get(f.name).asInstanceOf[DBObject])

    def getPrimitiveArray[T](key:String)(implicit con:AnyRefConverter[T]): Seq[T] =
      getAnyArrayType[T](asSeqContainer(key))(element => con.convert(element))

    def getPrimitiveArray[T](f:Field[Seq[T]])(implicit con:AnyRefConverter[T]): Seq[T] = getPrimitiveArray[T](f.name)

    def getId: MongoObjectId = MongoObjectId(dbo.get("_id").asInstanceOf[ObjectId])

    def getMongoArray[T <: DomainObject : MongoToDomain](key:String): Seq[T] = getAnyArrayType[T](asSeqContainer(key))(
      element => implicitly[MongoToDomain[T]].apply(element.asInstanceOf[DBObject]))

    def getMongoArray[T <: DomainObject : MongoToDomain](f:Field[T]): Seq[T] = getMongoArray[T](f.name)

    def putPrimitive[T](key:String, value:T): MongoObject = { putPrimitiveOfAnyType[T](key, value) }

    def putPrimitive[T](fv:FieldValue[T]): MongoObject = { putPrimitiveOfAnyType[T](fv.name, fv.value) }

    def putMongo(key:String, mongo:MongoObject): MongoObject = putAnyMongo(key, mongo)

    def putMongo[T <: DomainObject <% MongoObject](fv:FieldValue[T]): MongoObject = { putAnyMongo(fv.name, fv.value) }

    //todo: a DomainObject should already have a FieldValue[MongoObjectId]
    def putId(id:MongoObjectId): MongoObject = { dbo.put("_id", id.toObjectId); copyMongoObject }

    def merge(mo:MongoObject): MongoObject = { dbo.putAll(mo.toDBObject); copyMongoObject }

    def putMongoArray(key:String, values:Seq[MongoObject]): MongoObject = putAnyMongoArray(key, values)

    def putMongoArray[T <: DomainObject <% MongoObject](fv:FieldValue[Seq[T]]): MongoObject = putAnyMongoArray(fv.name,
      fv.value.map(implicitly[MongoObject](_)))

    //TODO: Test
    def putPrimitiveArray[T](key:String, values:Seq[T]): MongoObject = putPrimitiveArrayOfAnyType[T](key, values)

    //TODO: Test
    def putPrimitiveArray[T](fv:FieldValue[Seq[T]]): MongoObject = putPrimitiveArrayOfAnyType[T](fv.name, fv.value)

//    private[akuru] def getPrimitiveOfAnyType[T](key: => String)(implicit con:AnyRefConverter[T]): T = {
//      getAnyArrayType[T](asSingleElementContainer(key))(element => con.convert(element)).head
//    }

    private[akuru] def getAnyArrayType[T](container: => Seq[AnyRef])(f: AnyRef => T): Seq[T] = {
      val buffer = new ListBuffer[T]
      for(element <- container) {
        buffer += f(element)
      }

      buffer.toSeq
    }

    private[akuru] def asSeqContainer(key: String): Seq[AnyRef] = {
      import scala.collection.JavaConversions._
      dbo.get(key).asInstanceOf[BasicDBList].toSeq
    }

    private[akuru] def asSingleElementContainer(key: String): Seq[AnyRef] =  Seq(dbo.get(key))

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

  object MongoObject extends
      SetFuncs with
      PullFuncs with
      Funcs
}
