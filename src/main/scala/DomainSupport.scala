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

    def apply(value:T): Value = Value(value)

    def === (value:T) : Value = apply(value)

    final case class Value(value:T) {
      val name = field.name
      lazy val field = FieldType.this  //we need this lazy as it should be accessed only after Field instantiation.
    }
  }

  case class Field[O <: DomainObject, T](override val name:String) extends FieldType[O, T]

  case class NestedField[O <: DomainObject, T](parentField:FieldType[O, _], override val name:String) extends FieldType[O, T] {
    //override val name = parentField.name + "." + _name
  }

  class Owner[O <: DomainObject] {
    def createField[T](name:String): Field[O, T] = Field[O, T](name)
    def createNestedField[T](parentField:FieldType[O, _], name:String): NestedField[O, T] = NestedField[O, T](parentField, name)
  }

  trait DomainObject
  trait NestedObject

  sealed abstract class Template[O <: DomainObject]

  abstract class NestedTemplate[O <: DomainObject, N <: NestedObject](parentField:FieldType[O, _]) extends Template[O] {
    def field[T](name:String): NestedField[O, T] = new Owner[O].createNestedField[T](parentField, name)

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