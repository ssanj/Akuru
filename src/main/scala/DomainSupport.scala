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

  abstract class Template[O <: DomainObject] {
    def field[T](name:String): Field[O, T] = new Owner[O].createField[T](name)
    def nestedField[T](parentField:FieldType[O, _], name:String): NestedField[O, T] = new Owner[O].createNestedField[T](parentField, name)
  }

  abstract class DomainTemplate[O <: DomainObject] extends Template[O] {
    private val idKey = "_id"
    val idField: Field[O, MID] = new Owner[O].createField[MID](idKey)
    val defaultId: idField.Value = idField === None
  }

  trait CollectionName[T <: DomainObject] {
    val name:String
  }

  def putId(id:MID): MongoObject =  foldOption(id)(empty)(id => empty.putId(id))
}