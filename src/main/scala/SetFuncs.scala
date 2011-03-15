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

  def set[O <: DomainObject](update: MongoUpdateObject[O]): MongoUpdateObject[O] =  toMongoUpdateObject[O]($funcMongo(functionName, update.value))

  def set[O <: DomainObject, T : ClassManifest](fv: FieldValue[O, T]): MongoUpdateObject[O] = toMongoUpdateObject[O](anyFunction1[O, T](functionName, fv))
}