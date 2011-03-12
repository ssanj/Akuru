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

  case class Field[T](name:String) {

    def apply(value:T): Value = Value(value)

    def === (value:T) : Value = apply(value)

    final case class Value(value:T) {
      val name = field.name
      lazy val field = Field.this  //we need this lazy as it should be accessed only after Field instantiation.
    }
  }

  trait DomainObject {
    val id:DomainObject.idField.Value
  }

  abstract class DomainTemplate[T <: DomainObject]

  object DomainObject {
    val idField = Field[MID]("_id")
    val defaultId = idField === None
  }

  trait CollectionName[T <: DomainObject] {
    val name:String
  }

  def putDomainId(domain:DomainObject): MongoObject =  foldOption(domain.id.value)(empty)(id => empty.putId(id))
}