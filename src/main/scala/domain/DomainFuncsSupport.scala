/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package domain

import akuru.MongoObject.empty
import akuru.Tools

trait DomainFuncsSupport { this: DomainTypeSupport with Tools =>

  def putId(id:MID): MongoObject =  foldOption(id)(empty)(id => empty.putId(id))
}