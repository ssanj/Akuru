/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes.MongoObjectId
import MongoTypes.MongoObject
import MongoTypes.MongoObject.empty

trait DomainSupport { this:Tools =>

  case class FieldValue[T](field:Field[T], value:T) {
    val name = field.name
  }

  case class Field[T](name:String) {
    def apply(value:T): FieldValue[T] = FieldValue[T](this, value)
  }



  type MID = Option[MongoObjectId]

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

  type MongoToDomain[T <: DomainObject] = MongoObject => T

  type DomaintToMongo[T <: DomainObject] = T => MongoObject

  def putDomainId(domain:DomainObject): MongoObject =  foldOption(domain.id.value)(empty)(id => empty.putId(id))
}