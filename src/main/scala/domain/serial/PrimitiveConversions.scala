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

  implicit object StringArrayToMongo extends MakeItMongo[Seq[String]]

  implicit object IntArrayToMongo extends MakeItMongo[Seq[Int]]

  implicit object LongArrayToMongo extends MakeItMongo[Seq[Long]]

  implicit object CharArrayToMongo extends MakeItMongo[Seq[Char]]

  implicit object ShortArrayToMongo extends MakeItMongo[Seq[Short]]

  implicit object ByteArrayToMongo extends MakeItMongo[Seq[Byte]]

  implicit object FloatArrayToMongo extends MakeItMongo[Seq[Float]]

  implicit object DoubleArrayToMongo extends MakeItMongo[Seq[Double]]

  implicit object BooleanArrayToMongo extends MakeItMongo[Seq[Boolean]]

  abstract class MakeItMongo[T : ClassManifest] extends ToMongo[T] {
    def convert[O <: DomainObject](fv:FieldValue[O, T]): MongoObject = empty.putAnything[O, T](fv)
  }
}