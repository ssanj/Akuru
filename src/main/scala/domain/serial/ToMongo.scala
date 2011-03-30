/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package domain
package serial

import MongoTypes.{ DomainObject => DO }

trait ToMongo[T] {
  def convert[O <: DO](fv:FieldValue[O, T]): MongoObject
}


trait FromMongo[T] {
  def convert(mongo:MongoObject): Option[T]
}