/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._

trait DomainSupport {

  trait Ided {
    val id:Option[MongoObjectId]
  }

  trait DomainObject extends Ided

  trait CollectionName[T] {
    val name:String
  }
}