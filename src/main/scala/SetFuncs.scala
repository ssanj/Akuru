/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes.MongoObject
import MongoTypes.FieldValue

trait SetFuncs { this:Funcs =>

  def set(col:String, value:AnyRef): MongoObject =  $funcPrimitive("$set", col, value)

  def set(col:String, value:MongoObject): MongoObject =  $funcMongo("$set", col, value)

  def set[T](fv:FieldValue[T]): MongoObject = $funcMongo("$set", fieldToMongo1[T](fv))

  def set[R, T](fv1:FieldValue[R], fv2:FieldValue[T]): MongoObject = $funcMongo("$set", fieldToMongo2[R,T](fv1, fv2))

  def set[R, S, T](fv1:FieldValue[R], fv2:FieldValue[S], fv3:FieldValue[T]): MongoObject = $funcMongo("$set", fieldToMongo3[R,S,T](fv1, fv2, fv3))

}