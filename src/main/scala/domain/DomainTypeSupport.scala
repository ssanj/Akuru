/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package domain

trait DomainTypeSupport {

  import org.bson.types.ObjectId

  type MID = Option[MongoObjectId]

  type MongoToDomain[T <: DomainObject] = MongoObject => Option[T]

  type DomainToMongo[T <: DomainObject] = T => MongoObject

  type MongoToNested[T <: NestedObject] = MongoObject => Option[T]

  sealed trait AkuruObject

  abstract class DomainObject extends AkuruObject with Product

  abstract class NestedObject extends AkuruObject with Product

  trait Primitive[T] extends AkuruObject

  implicit object StringP extends Primitive[String]

  implicit object ByteP extends Primitive[Byte]

  implicit object ShortP extends Primitive[Short]

  implicit object IntP extends Primitive[Int]

  implicit object LongP extends Primitive[Long]

  implicit object BooleanP extends Primitive[Boolean]

  implicit object DoubleP extends Primitive[Double]

  implicit object FloatP extends Primitive[Float]

  implicit object CharP extends Primitive[Char]

  implicit object ObjectIdP extends Primitive[ObjectId]

  trait CollectionName[T <: DomainObject] {
    val name:String
  }
}