/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package domain

import akuru.MongoObject.empty
import akuru.Tools._

trait DomainFuncsSupport {

  def putId(id:FieldValue[_ <: DomainObject, MID]): MongoObject =  foldOption(id.value)(empty)(value => empty.putId(value))
}