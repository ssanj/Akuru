/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoObject._
import MongoTypes.FieldValueJoiner
import MongoTypes.MongoJoinerValue

trait OperatorTypes {

  case class OperatorObject[O <: DomainObject, T <% Number](f:Field[O, T]) {

    def lt(t1:T): FieldValueJoiner[O] = operate(f, t1, "$lt")

    def <(t1:T): FieldValueJoiner[O] = lt(t1)

    def lte(t1:T): FieldValueJoiner[O] = operate(f, t1, "$lte")

    def <=(t1:T): FieldValueJoiner[O] = lte(t1)

    def gt(t1:T): FieldValueJoiner[O] = operate(f, t1, "$gt")

    def >(t1:T): FieldValueJoiner[O] = gt(t1)

    def gte(t1:T): FieldValueJoiner[O] = operate(f, t1, "$gte")

    def >=(t1:T): FieldValueJoiner[O] = gte(t1)

    def between(t1:T, t2:T): FieldValueJoiner[O] = createFieldValueJoiner(
      mongo.putMongo(f.name, mongo.merge("$gte" -> t1).merge(mongo.merge("$lte" -> t2))))

    def |<>|(t1:T, t2:T): FieldValueJoiner[O] = between(t1, t2)

    private def operate(f:Field[O, T], value: => T, func: => String): FieldValueJoiner[O] =
      createFieldValueJoiner($funcPrimitive(f.name, func, value))

    private def createFieldValueJoiner(mo: MongoObject): FieldValueJoiner[O] = FieldValueJoiner[O](MongoJoinerValue[O](mo))
  }
}