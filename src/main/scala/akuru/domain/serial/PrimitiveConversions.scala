/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru
package domain
package serial

import MongoObject.empty

/**
 * Type classes for converting any primitive object or an array of, into a corresponding MongoObject.
 */
trait PrimitiveConversions {

  implicit object StringToMongo extends PrimitiveMongo[String]

  implicit object IntToMongo extends PrimitiveMongo[Int]

  implicit object LongToMongo extends PrimitiveMongo[Long]

  implicit object CharToMongo extends PrimitiveMongo[Char]

  implicit object ShortToMongo extends PrimitiveMongo[Short]

  implicit object ByteToMongo extends PrimitiveMongo[Byte]

  implicit object FloatToMongo extends PrimitiveMongo[Float]

  implicit object DoubleToMongo extends PrimitiveMongo[Double]

  implicit object BooleanToMongo extends PrimitiveMongo[Boolean]

  implicit object MongoObjectIdToMongo extends PrimitiveId

  implicit object StringArrayToMongo extends PrimitiveArray[String]

  implicit object IntArrayToMongo extends PrimitiveArray[Int]

  implicit object LongArrayToMongo extends PrimitiveArray[Long]

  implicit object CharArrayToMongo extends PrimitiveArray[Char]

  implicit object ShortArrayToMongo extends PrimitiveArray[Short]

  implicit object ByteArrayToMongo extends PrimitiveArray[Byte]

  implicit object FloatArrayToMongo extends PrimitiveArray[Float]

  implicit object DoubleArrayToMongo extends PrimitiveArray[Double]

  implicit object BooleanArrayToMongo extends PrimitiveArray[Boolean]

  implicit object OptionalMongoObjectIdToMongo extends PrimitiveOptionId

  abstract class PrimitiveMongo[T : ClassManifest] extends ToMongo[T] {
    def convert[O <: DomainObject](fv:FieldValue[O, T]): MongoObject = MongoObject(Map(fv.name -> fv.value.asInstanceOf[AnyRef]))
  }

  abstract class PrimitiveArray[T : ClassManifest] extends ToMongo[Seq[T]] {
    def convert[O <: DomainObject](fv:FieldValue[O, Seq[T]]): MongoObject = MongoObject(Map(fv.name -> convertToJavaList[T](fv.value)))
  }

  class PrimitiveOptionId extends ToMongo[Option[MongoObjectId]] {
    import Tools._
    def convert[O <: DomainObject](fv:FieldValue[O, Option[MongoObjectId]]): MongoObject =
      fv.value fold(empty, v => MongoObject(Map(fv.name -> v.toObjectId)))
  }

  class PrimitiveId extends ToMongo[MongoObjectId] {
    def convert[O <: DomainObject](fv:FieldValue[O, MongoObjectId]): MongoObject = MongoObject(Map(fv.name -> fv.value.toObjectId))
  }

 private def convertToJavaList[T](values: Seq[T]): AnyRef = {
    import com.mongodb.BasicDBList
    import scala.collection.JavaConversions._
    val list:java.util.List[AnyRef] = values.toList.map(_.asInstanceOf[AnyRef])
    val bslist:BasicDBList = new BasicDBList()
    bslist.addAll(list)
    bslist
  }
}