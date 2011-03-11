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

  def set(update: MongoUpdateObject): MongoUpdateObject =  toMongoUpdateObject($funcMongo(functionName, update.value))

  def set[T : ClassManifest](fv: FieldValue[T]): MongoUpdateObject = toMongoUpdateObject(anyFunction1[T](functionName, fv))
}