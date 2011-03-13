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

  case class Field[O <: DomainObject, T](name:String) {

    def apply(value:T): Value = Value(value)

    def === (value:T) : Value = apply(value)

    final case class Value(value:T) {
      val name = field.name
      lazy val field = Field.this  //we need this lazy as it should be accessed only after Field instantiation.
    }
  }

  class Owner[O <: DomainObject] {
    def createField[T](name:String): Field[O, T] = Field[O, T](name)
  }

  trait DomainObject

  abstract class DomainTemplate[O <: DomainObject] {
    private val idKey = "_id"
    def field[T](name:String): Field[O, T] = new Owner[O].createField[T](name)
    val idField: Field[O, MID] = new Owner[O].createField[MID](idKey)
    val defaultId: idField.Value = idField === None
  }

  trait CollectionName[T <: DomainObject] {
    val name:String
  }

  def putId(id:MID): MongoObject =  foldOption(id)(empty)(id => empty.putId(id))
}