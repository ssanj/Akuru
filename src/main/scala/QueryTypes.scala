/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoObject
import MongoTypes.DomainToMongo
import MongoTypes.DomainObject

trait QueryTypes {

  sealed trait SortObject {
    val value:MongoObject
  }

  object SortOrder extends Enumeration {
    val ASC =  Value(1)
    val DSC  = Value(-1)
  }

  case class MongoSortObject(mo:MongoObject) extends SortObject {
    override val value:MongoObject = mo
  }

  sealed trait UpdateObject {
    val value:MongoObject
  }

  case class DomainUpdateObject[T <: DomainObject : DomainToMongo](domain:T) extends UpdateObject {
    override val value:MongoObject = implicitly[DomainToMongo[T]].apply(domain)
  }

  case class MongoUpdateObject(mo:MongoObject) extends UpdateObject {
    override val value:MongoObject = mo

    def and(other:MongoUpdateObject): MongoUpdateObject = MongoUpdateObject(mo.mergeDupes(other.value))
  }

  object UpdateObject {
    implicit def domainToUpdateObject[T <: DomainObject : DomainToMongo](value:T): UpdateObject = DomainUpdateObject[T](value)
    //we don't add an implicit def for mongo -> updateObject here as we want to limit it to specific mongo objects.
  }
}