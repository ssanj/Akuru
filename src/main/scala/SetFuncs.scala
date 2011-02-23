/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes.MongoObject
import MongoTypes.MongoUpdateObject

/**
 * { $set : { field : value } }
 * sets field to value. All datatypes are supported with $set.
 */
trait SetFuncs { this:Funcs =>

  private object SetFuncs {
    val functionName = "$set"
  }

  import SetFuncs._

  def set(value:MongoObject): MongoUpdateObject =  toMongoUpdateObject($funcMongo(functionName, value))

  def set[T]: FieldValue[T] => MongoUpdateObject = fv => toMongoUpdateObject(anyFunction1[T](functionName)(fv))
}