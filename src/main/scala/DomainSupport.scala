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


  case class FieldValue[T](field:Field[T], value:T) {
    val name = field.name
  }

  case class Field[T](name:String) {
    def apply(value:T): FieldValue[T] = FieldValue[T](this, value)
  }

  trait DomainObject {
    val id:FieldValue[MID]
  }

  object DomainObject {
    val idField = Field[MID]("_id")

    val defaultId:FieldValue[MID] = idField.apply(None)
  }

  trait CollectionName[T <: DomainObject] {
    val name:String
  }

  def putDomainId(domain:DomainObject): MongoObject =  foldOption(domain.id.value)(empty)(id => empty.putId(id))
}