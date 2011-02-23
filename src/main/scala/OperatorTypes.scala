/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoObject
import MongoTypes.MongoObject._

trait OperatorTypes {

  case class OperatorObject[T <% Number](f:Field[T]) {

    def lt(t1:T): MongoObject = operate[T](f, t1, "$lt")

    def <(t1:T): MongoObject = lt(t1)

    def lte(t1:T): MongoObject = operate[T](f, t1, "$lte")

    def <=(t1:T): MongoObject = lte(t1)

    def gt(t1:T): MongoObject = operate[T](f, t1, "$gt")

    def >(t1:T): MongoObject = gt(t1)

    def gte(t1:T): MongoObject = operate[T](f, t1, "$gte")

    def >=(t1:T): MongoObject = gte(t1)

    def between(t1:T, t2:T): MongoObject = mongo.putMongo(f.name, mongo.putPrimitive("$gte", t1).merge(mongo.putPrimitive("$lte", t2)))

    def |<>|(t1:T, t2:T): MongoObject = between(t1, t2)

    private def operate[T <% Number](f:Field[T], value: => T, func:String): MongoObject =  $funcPrimitive(f.name, func, value)
  }
}