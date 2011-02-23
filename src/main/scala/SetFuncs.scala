/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes.MongoObject
import MongoTypes.MongoUpdateObject
import MongoTypes.FieldValue

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

  def set[R, T]: (FieldValue[R], FieldValue[T]) => MongoUpdateObject = (fv1, fv2) => toMongoUpdateObject(anyFunction2[R, T](functionName)(fv1, fv2))

  def set[R, S, T]: (FieldValue[R], FieldValue[S], FieldValue[T]) => MongoUpdateObject = (fv1, fv2, fv3) =>
    toMongoUpdateObject(anyFunction3[R, S, T](functionName)(fv1, fv2, fv3))
}