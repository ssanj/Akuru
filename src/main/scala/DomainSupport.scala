/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes.MongoObjectId
import MongoTypes.MongoObject
import MongoTypes.MongoObject.empty

trait DomainSupport { this:Tools =>

  trait Ided {
    val id:Option[MongoObjectId]
  }

  trait DomainObject extends Ided

  trait CollectionName[T <: DomainObject] {
    val name:String
  }

  def putDomainId(domain:DomainObject): MongoObject =  foldOption(domain.id)(empty)(id => empty.putId(id))
}