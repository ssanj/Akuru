/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

trait QueryTypes {

  import MongoObject.mongo

  sealed abstract class QueryJoiner[O <: DomainObject] {
    def splat: MongoObject
  }

  private[akuru] case class FieldValueQueryJoiner[O <: DomainObject, T : ClassManifest](fv: FieldValue[O, T]) extends QueryJoiner[O] {
    def splat: MongoObject = mongo.putAnything[O, T](fv)
  }

  private[akuru] case class MongoQueryJoiner[O <: DomainObject](mo: MongoObject) extends QueryJoiner[O] {
    def splat: MongoObject = mo
  }

  case class Query[O <: DomainObject]private[akuru] (join: QueryJoiner[O]) {
    def and2[S : ClassManifest](fv2:FieldValue[O, S]): Query[O] =
      Query[O](MongoQueryJoiner[O](join.splat.putAnything[O, S](fv2)))

    def and2(another:Query[O]): Query[O] = Query[O](MongoQueryJoiner[O](join.splat.merge(another.splat)))

    def splat: MongoObject = join.splat
  }

  case class MongoJoiner(mo:MongoObject) {

    def splat: MongoObject = mo
  }
}