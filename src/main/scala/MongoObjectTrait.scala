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

    def getPrimitive[T : AnyRefConverter](key:String): T = getAnyArrayType[T](asSingleElementContainer(key))(getPrimitiveConverter[T]).head

    def getPrimitive[T : AnyRefConverter](f:Field[T]): T = getPrimitive[T](f.name)

    def getPrimitiveArray[T : AnyRefConverter](key:String): Seq[T] = getAnyArrayType[T](asSeqContainer(key))(getPrimitiveConverter[T])

    def getPrimitiveArray[T](f:Field[Seq[T]])(implicit con:AnyRefConverter[T]): Seq[T] = getPrimitiveArray[T](f.name)

    //TODO: Test
    def getMongo[T <: DomainObject : MongoToDomain](f:Field[T]): T = getAnyArrayType[T](asSingleElementContainer(f.name))(mongoConverter[T]).head

    def getMongoArray[T <: DomainObject : MongoToDomain](key:String): Seq[T] = getAnyArrayType[T](asSeqContainer(key))(mongoConverter[T])

    def getMongoArray[T <: DomainObject : MongoToDomain](f:Field[T]): Seq[T] = getMongoArray[T](f.name)

    def getId: MongoObjectId = MongoObjectId(dbo.get("_id").asInstanceOf[ObjectId])

    def putPrimitive[T](key:String, value:T): MongoObject = { putAnyArray(asJavaObject)(key, Seq(value.asInstanceOf[AnyRef])) }

    def putPrimitive[T](fv:FieldValue[T]): MongoObject = { putPrimitive(fv.name, fv.value) }

    def putMongo(key:String, mongo:MongoObject): MongoObject = putAnyArray(asJavaObject)(key, Seq(mongo.toDBObject))

    def putMongo[T <: DomainObject <% MongoObject](fv:FieldValue[T]): MongoObject = { putMongo(fv.name, fv.value) }

    //todo: a DomainObject should already have a FieldValue[MongoObjectId]
    def putId(id:MongoObjectId): MongoObject = { dbo.put("_id", id.toObjectId); copyMongoObject }

    def merge(mo:MongoObject): MongoObject = { dbo.putAll(mo.toDBObject); copyMongoObject }

    def putMongoArray(key:String, values:Seq[MongoObject]): MongoObject = putAnyArray(asJavaList)(key,
      values.map(_.toDBObject))

    def putMongoArray[T <: DomainObject <% MongoObject](fv:FieldValue[Seq[T]]): MongoObject = putMongoArray(fv.name,
      fv.value.map(implicitly[MongoObject](_)))

    //TODO: Test
    def putPrimitiveArray[T](key:String, values: => Seq[T]): MongoObject = putAnyArray(asJavaList)(key, values.map(_.asInstanceOf[AnyRef]))

    //TODO: Test
    def putPrimitiveArray[T](fv:FieldValue[Seq[T]]): MongoObject = putPrimitiveArray[T](fv.name, fv.value)

    private[akuru] def mongoConverter[T <: DomainObject : MongoToDomain](element: AnyRef): T =
      implicitly[MongoToDomain[T]].apply(element.asInstanceOf[DBObject])

    private[akuru] def getPrimitiveConverter[T : AnyRefConverter](element:AnyRef): T = implicitly[AnyRefConverter[T]].convert(element)

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

    private[akuru] def putAnyArray(f: Seq[AnyRef] => AnyRef)(key: => String, values: => Seq[AnyRef]): MongoObject = {
      dbo.put(key, f(values))
      copyMongoObject
    }

    private[akuru] def asJavaList(values: Seq[AnyRef]): AnyRef = {
      import scala.collection.JavaConversions._
      val list:java.util.List[AnyRef] = values.toList
      list
    }

    private[akuru] def asJavaObject(values: Seq[AnyRef]): AnyRef =  values.head

    private[akuru] def toDBObject: DBObject = new BasicDBObject(dbo.toMap)

    private[akuru] def copyMongoObject: MongoObject = MongoObject(toDBObject)
  }

  object MongoObject extends
      SetFuncs with
      PullFuncs with
      Funcs
}
