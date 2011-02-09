/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._
import MongoTypes.MongoObject.empty

trait DomainSupport {

  trait Ided {
    val id:Option[MongoObjectId]
  }

  trait DomainObject extends Ided

  trait CollectionName[T] {
    val name:String
  }

  def putDomainId(domain:DomainObject): MongoObject =  foldOption(domain.id)(empty)(id => empty.putId(id))
}