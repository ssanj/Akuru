/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._

trait DomainSupport {

  case class NamedField[T](name:String)

  trait CommonTypes {
      type idType = Option[MongoObjectId]
      object id_nf extends NamedField[idType]("_id")
  }

  trait DomainObject extends CommonTypes {
    val id:idType
  }

  trait CollectionName[T] {
    val name:String
  }
}