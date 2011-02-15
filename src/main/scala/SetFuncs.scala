/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes.MongoObject

/**
 * { $set : { field : value } }
 * sets field to value. All datatypes are supported with $set.
 */
trait SetFuncs { this:Funcs =>

  private object SetFuncs {
    val functionName = "$set"
  }

  import SetFuncs._

  def set(value:MongoObject*): MongoObject =  $funcMongo(functionName, combine(value:_*))

  def set[T] = anyFunction1[T](functionName)

  def set[R, T] = anyFunction2[R, T](functionName)

  def set[R, S, T] = anyFunction3[R, S, T](functionName)
}