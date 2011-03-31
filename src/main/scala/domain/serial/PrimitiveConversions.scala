/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru
package domain
package serial

import MongoObject.empty
import MongoTypes.{DomainObject => DO}
import Tools._

/**
 * Type classes for converting any primitive object or an array of, into a corresponding MongoObject.
 */
trait PrimitiveConversions {

  implicit object StringToMongo extends MakeItMongo[String]

  implicit object IntToMongo extends MakeItMongo[Int]

  implicit object LongToMongo extends MakeItMongo[Long]

  implicit object CharToMongo extends MakeItMongo[Char]

  implicit object ShortToMongo extends MakeItMongo[Short]

  implicit object ByteToMongo extends MakeItMongo[Byte]

  implicit object FloatToMongo extends MakeItMongo[Float]

  implicit object DoubleToMongo extends MakeItMongo[Double]

  implicit object BooleanToMongo extends MakeItMongo[Boolean]

  implicit object MongoObjectIdToMongo extends MakeItMongo[MongoObjectId]

  implicit object StringArrayToMongo extends MakeItMongoArray[String]

  implicit object IntArrayToMongo extends MakeItMongoArray[Int]

  implicit object LongArrayToMongo extends MakeItMongoArray[Long]

  implicit object CharArrayToMongo extends MakeItMongoArray[Char]

  implicit object ShortArrayToMongo extends MakeItMongoArray[Short]

  implicit object ByteArrayToMongo extends MakeItMongoArray[Byte]

  implicit object FloatArrayToMongo extends MakeItMongoArray[Float]

  implicit object DoubleArrayToMongo extends MakeItMongoArray[Double]

  implicit object BooleanArrayToMongo extends MakeItMongoArray[Boolean]

  implicit object OptionalMongoObjectIdToMongo extends MakeItMongoId

  abstract class MakeItMongo[T : ClassManifest] extends ToMongo[T] {
    def convert[O <: DO](fv:FieldValue[O, T]): MongoObject = MongoObject(Map(fv.name -> fv.value.asInstanceOf[AnyRef]))
  }

  abstract class MakeItMongoArray[T : ClassManifest] extends ToMongo[Seq[T]] {
    def convert[O <: DO](fv:FieldValue[O, Seq[T]]): MongoObject = MongoObject(Map(fv.name -> convertToJavaList[T](fv.value)))
  }

  class MakeItMongoId extends ToMongo[Option[MongoObjectId]] {
    def convert[O <: DO](fv:FieldValue[O, Option[MongoObjectId]]): MongoObject =  fv.value fold(empty, v => MongoObject(Map(fv.name -> v.toObjectId)))
  }

 private def convertToJavaList[T](values: Seq[T]): AnyRef = {
   import scala.collection.JavaConversions.asJavaList
   import com.mongodb.BasicDBList
   val list:java.util.List[AnyRef] = values.toList.map(_.asInstanceOf[AnyRef])
    val bslist:BasicDBList = new BasicDBList()
    bslist.addAll(list)
    bslist
  }
}