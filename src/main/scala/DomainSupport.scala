/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes.MongoObject.empty

trait DomainSupport { this:Tools =>

  type MID = Option[MongoObjectId]

  type MongoToDomain[T <: DomainObject] = MongoObject => Option[T]

  type DomainToMongo[T <: DomainObject] = T => MongoObject

  type NestedToMongo[T <: NestedObject] = T => MongoObject

  type MongoToNested[T <: NestedObject] = MongoObject => Option[T]

  sealed abstract class FieldType[O <: DomainObject, T] {
    val name:String

    val path:String

    def apply(value:T): Value = Value(value)

    def === (value:T) : Value = apply(value)

    final case class Value(value:T) {
      val name = field.name
      val path = field.path
      lazy val field = FieldType.this  //we need this lazy as it should be accessed only after Field instantiation.
    }
  }

  /**
   * A DomainTemplateField is any field that is within a DomainTemplate.
   */
  sealed abstract class DomainTemplateField[O <: DomainObject, T] extends FieldType[O, T] {
    override val path:String = name
  }

  //tood: Rename to PrimitiveField
  final case class Field[O <: DomainObject, T](override val name:String) extends DomainTemplateField[O, T]

  final case class EmbeddedField[O <: DomainObject, T <: NestedObject](override val name:String) extends DomainTemplateField[O, T]

  final case class ArrayField[O <: DomainObject, T](override val name:String) extends DomainTemplateField[O, Seq[T]]

  final case class EmbeddedArrayField[O <: DomainObject, T <: NestedObject](override val name:String) extends DomainTemplateField[O, Seq[T]]

  sealed abstract class NestedTemplateField[O <: DomainObject, T] extends FieldType[O, T] {
    val parentField:FieldType[O, _]

    private object Constants {
      val pathSeparator = "."
    }

    import Constants._
    override val path = findPath(parentField) + pathSeparator + name

    private def findPath(ft:FieldType[O, _]): String =
      ft match {
        case f:DomainTemplateField[_, _] => f.name
        case n:NestedTemplateField[_, _] => findPath(n.parentField)  + pathSeparator + n.name
      }
  }

  final case class NestedField[O <: DomainObject, T](override val parentField:FieldType[O, _], override val name:String)
          extends NestedTemplateField[O, T]

  final case class NestedEmbeddedField[O <: DomainObject, T <: NestedObject](override val parentField:FieldType[O, _], override val name:String)
          extends NestedTemplateField[O, T]

  final case class NestedArrayField[O <: DomainObject, T](override val parentField:FieldType[O, _], override val name:String)
          extends NestedTemplateField[O, Seq[T]]

  final case class NestedEmbeddedArrayField[O <: DomainObject, T <: NestedObject](override val parentField:FieldType[O, _], override val name:String)
          extends NestedTemplateField[O, Seq[T]]

  class Owner[O <: DomainObject] {
    def createField[T](name:String): Field[O, T] = Field[O, T](name)
    def createArrayField[T](name:String): ArrayField[O, T] = ArrayField[O, T](name)
    def embeddedField[T <: NestedObject](name:String): EmbeddedField[O, T] = EmbeddedField[O, T](name)
    def embeddedArrayField[T <: NestedObject](name:String): EmbeddedArrayField[O, T] = EmbeddedArrayField[O, T](name)

    def createNestedField[T](parentField:FieldType[O, _], name:String): NestedField[O, T] = NestedField[O, T](parentField, name)
    def createNestedArrayField[T](parentField:FieldType[O, _], name:String): NestedArrayField[O, T] = NestedArrayField[O, T](parentField, name)
    def createNestedEmbeddedField[T <: NestedObject](parentField:FieldType[O, _], name:String): NestedEmbeddedField[O, T] =
      NestedEmbeddedField[O, T](parentField, name)
    def createNestedEmbeddedArrayField[T <: NestedObject](parentField:FieldType[O, _], name:String): NestedEmbeddedArrayField[O, T] =
      NestedEmbeddedArrayField[O, T](parentField, name)
  }

  sealed trait AkuruObject
  abstract class DomainObject extends AkuruObject
  abstract class NestedObject extends AkuruObject

  sealed abstract class Template[O <: DomainObject]

  abstract class NestedTemplate[O <: DomainObject, N <: NestedObject](parentField:FieldType[O, N]) extends Template[O] {

    //TODO: Find a better way to do this.
    def this(parentField:NestedArrayField[O, N]) = this(NestedField[O, N](parentField.parentField, parentField.name))

    def field[T](name:String): NestedField[O, T] = new Owner[O].createNestedField[T](parentField, name)

    def arrayField[T](name:String): NestedArrayField[O, T] = new Owner[O].createNestedArrayField[T](parentField, name)

    def nestedToMongoObject(nested: N): MongoObject

    def mongoToNested(mo:MongoObject): Option[N]

    implicit def _nestedToMongoObject(nested: N): MongoObject = nestedToMongoObject(nested)

    implicit def _mongoToNested(mo:MongoObject): Option[N] = mongoToNested(mo)
  }

  abstract class DomainTemplate[O <: DomainObject] extends Template[O] {
    private val idKey = "_id"
    val idField: Field[O, MID] = new Owner[O].createField[MID](idKey)
    val defaultId: idField.Value = idField === None

    def field[T](name:String): Field[O, T] = new Owner[O].createField[T](name)

    def arrayField[T](name:String): ArrayField[O, T] = new Owner[O].createArrayField[T](name)

    val collectionName:String

    def domainToMongoObject(domain: O): MongoObject

    def mongoToDomain(mo:MongoObject): Option[O]

    implicit def _domainToMongoObject(domain: O): MongoObject = domainToMongoObject(domain)

    implicit def _mongoToDomain(mo:MongoObject): Option[O] = mongoToDomain(mo)


    implicit object DomainCollectionName extends CollectionName[O] {
      override lazy val name:String = collectionName
    }
  }

  trait CollectionName[T <: DomainObject] {
    val name:String
  }

  def putId(id:MID): MongoObject =  foldOption(id)(empty)(id => empty.putId(id))
}