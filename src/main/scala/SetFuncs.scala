/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

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

  def set[T : ClassManifest](fv: FieldValue[T]): MongoUpdateObject = toMongoUpdateObject(anyFunction1[T](functionName, fv))
}