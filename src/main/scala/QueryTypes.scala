/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

trait QueryTypes extends SortTypes with UpdateTypes {

  import MongoObject.mongo

  sealed abstract class JoinerValue[O <: DomainObject] {
    def done: MongoObject
  }

  case class FieldValueJoinerValue[O <: DomainObject, T : ClassManifest](fv: FieldValue[O, T]) extends JoinerValue[O] {
    def done: MongoObject = mongo.putAnything[O, T](fv)
  }

  case class MongoJoinerValue[O <: DomainObject](mo: MongoObject) extends JoinerValue[O] {
    def done: MongoObject = mo
  }

  case class FieldValueJoiner[O <: DomainObject](join: JoinerValue[O]) {
    def and2[S : ClassManifest](fv2:FieldValue[O, S]): FieldValueJoiner[O] =
      FieldValueJoiner[O](MongoJoinerValue[O](join.done.putAnything[O, S](fv2)))

    def and2(another:FieldValueJoiner[O]): FieldValueJoiner[O] = FieldValueJoiner[O](MongoJoinerValue[O](join.done.merge(another.done)))

    def done: MongoObject = join.done
  }

  case class MongoJoiner(mo:MongoObject) {

    def and(another:MongoObject): MongoJoiner = MongoJoiner(mo.merge(another))

    def done: MongoObject = mo
  }
}