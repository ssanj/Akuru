/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.FieldValue
import MongoTypes.Field
import MongoTypes.MongoObject
import MongoTypes.MongoObject._

trait MongoOperators { this:Funcs =>

  def lt[T <% Number](fv:FieldValue[T]): MongoObject = operate[T](fv, "$lt")

  def lte[T <% Number](fv:FieldValue[T]): MongoObject = operate[T](fv, "$lte")

  def gt[T <% Number](fv:FieldValue[T]): MongoObject = operate[T](fv, "$gt")

  def gte[T <% Number](fv:FieldValue[T]): MongoObject = operate[T](fv, "$gte")

  def between[T <% Number](fv:FieldValue[T], upper:T): MongoObject = {
    mongo.putMongo(fv.name, mongo.putPrimitive("$gte", fv.value).merge(mongo.putPrimitive("$lte", upper)))
  }

  private def operate[T <% Number](fv:FieldValue[T], func:String): MongoObject =  $funcPrimitive(fv.name, func, fv.value)
}