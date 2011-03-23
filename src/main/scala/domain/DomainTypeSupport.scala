/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package domain

trait DomainTypeSupport {

  type MID = Option[MongoObjectId]

  type MongoToDomain[T <: DomainObject] = MongoObject => Option[T]

  type DomainToMongo[T <: DomainObject] = T => MongoObject

  type NestedToMongo[T <: NestedObject] = T => MongoObject

  type MongoToNested[T <: NestedObject] = MongoObject => Option[T]

  sealed trait AkuruObject

  abstract class DomainObject extends AkuruObject

  abstract class NestedObject extends AkuruObject

  sealed trait Primitive[T] extends AkuruObject

  implicit object StringP extends Primitive[String]

  implicit object ByteP extends Primitive[Byte]

  implicit object ShortP extends Primitive[Short]

  implicit object IntP extends Primitive[Int]

  implicit object LongP extends Primitive[Long]

  implicit object BooleanP extends Primitive[Boolean]

  implicit object DoubleP extends Primitive[Double]

  implicit object FloatP extends Primitive[Float]

  implicit object CharP extends Primitive[Char]

  trait CollectionName[T <: DomainObject] {
    val name:String
  }
}