/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoCursor
import MongoTypes.MongoObject.sort

trait FindOneDSL extends MongoFunctions with Tools {

  def find: HowMany = new HowMany

  final class HowMany {

    def one[T <: DomainObject : CollectionName : MongoToDomain](template: => DomainTemplate[T]): QueryForSingleResult[T] = new QueryForSingleResult[T]

    def many[T <: DomainObject : CollectionName : MongoToDomain](template: => DomainTemplate[T]): QueryForMultipleResults[T]  =
      new QueryForMultipleResults[T]
  }

  final class QueryForSingleResult[T <: DomainObject : CollectionName : MongoToDomain] {
    def where(query: => MongoObject): Results[T] = new Results[T](query)
  }

  final class QueryForMultipleResults[T <: DomainObject : CollectionName : MongoToDomain] {

    def where(query: => MongoObject): ConstrainedBy[T] = new ConstrainedBy[T](query)
  }

  final class ConstrainedBy[T <: DomainObject : CollectionName : MongoToDomain](query: => MongoObject) {
    def constraintOf(bc: Constraint[T]): MultipleResults[T] = new MultipleResults[T](bc, query)

    def withResults(success: Seq[T] => Option[String]): UserFunction = new MultipleResults[T](All(), query).withResults(success)
  }

  sealed abstract class Constraint[T <: DomainObject : CollectionName : MongoToDomain] {
    def apply(): MongoCursor => MongoCursor
  }

  case class Limit[T <: DomainObject : CollectionName : MongoToDomain](n:Int) extends Constraint[T] {
    def apply(): MongoCursor => MongoCursor = mc => mc.limit(n)
  }

  case class Order[T <: DomainObject : CollectionName : MongoToDomain](fv:Field[T], order:SortOrder) extends Constraint[T] {
    def apply(): MongoCursor => MongoCursor = mc => mc.orderBy(sort(fv, order))
  }

  case class All[T <: DomainObject : CollectionName : MongoToDomain]() extends Constraint[T] {
    def apply(): MongoCursor => MongoCursor = mc => mc.all
  }

  final class MultipleResults[T <: DomainObject : CollectionName : MongoToDomain](constraint: Constraint[T], query: => MongoObject) {
    def withResults(success: Seq[T] => Option[String]): UserFunction = find[T](query)(constraint.apply())(success)
  }

  final class Results[T <: DomainObject : CollectionName : MongoToDomain](query: => MongoObject) {
    def withResults(success: T => Option[String]): ErrorsFindOne[T] = new ErrorsFindOne[T](query, success)
  }

  final class ErrorsFindOne[T <: DomainObject : CollectionName : MongoToDomain](query: => MongoObject, success: T => Option[String]) {
    def onError(error: => Unit): UserFunction = findOne[T](query)(success)(error)
  }
}
