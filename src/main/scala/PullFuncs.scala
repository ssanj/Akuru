/**
 * Copyright (c) $today.year Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoObject

trait PullFuncs { this:Funcs =>

  def pull(col:String, value:MongoObject): MongoObject =  $funcMongo("$pull", col, value)

  def pull(col:String, value:AnyRef): MongoObject =  $funcPrimitive("$pull", col, value)

}