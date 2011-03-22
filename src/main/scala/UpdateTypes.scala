/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru;

import Funcs.nestedPath

trait UpdateTypes {

  sealed trait UpdateObject[O <: DomainObject] {
    val value:MongoObject
  }

  case class DomainUpdateObject[T <: DomainObject : DomainToMongo](domain:T) extends UpdateObject[T] {
    override val value:MongoObject = implicitly[DomainToMongo[T]].apply(domain)
  }

  case class MongoUpdateObject[O <: DomainObject](mo:MongoObject) extends UpdateObject[O] {
    override val value:MongoObject = mo

    def &(other:MongoUpdateObject[O]): MongoUpdateObject[O] = MongoUpdateObject[O](mo.mergeMongoObjectValues(other.value))

    import MongoTypes.MongoObject.mongo
    def &[T : ClassManifest](other:FieldValue[O, T]): MongoUpdateObject[O] =
      MongoUpdateObject[O](mo.mergeMongoObjectValues(mongo.putAnything[O, T](other, nestedPath)))
  }

  object UpdateObject {
    implicit def domainToUpdateObject[T <: DomainObject : DomainToMongo](value:T): UpdateObject[T] = DomainUpdateObject[T](value)
    //we don't add an implicit def for mongo -> updateObject here as we want to limit it to specific mongo objects.
  }
}