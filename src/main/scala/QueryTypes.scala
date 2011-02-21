/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes._

trait QueryTypes {
  sealed trait UpdateObject {
    val value:MongoObject
  }

  case class DomainUpdateObject[T <: DomainObject : DomainToMongo](domain:T) extends UpdateObject {
    override val value:MongoObject = implicitly[DomainToMongo[T]].apply(domain)
  }

  case class MongoUpdateObject(mo:MongoObject) extends UpdateObject {
    override val value:MongoObject = mo
  }

  object UpdateObject {
    implicit def domainToUpdateObject[T <: DomainObject : DomainToMongo](value:T): UpdateObject = DomainUpdateObject[T](value)
    //we don't add an implicit def for mongo -> updateObject here as we want to limit it to specific mongo objects.
  }
}