/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoCursor
trait FindManyDSL { this:MongoFunctions with Tools with DSLTools =>

  final class QueryForMultipleResults[T <: DomainObject : CollectionName : MongoToDomain] {

    import MongoTypes.Query
    def where(fvj: => Query[T]): ConstrainedBy[T] = new ConstrainedBy[T](fvj.splat)
  }

  final class ConstrainedBy[T <: DomainObject : CollectionName : MongoToDomain](query: => MongoObject) {
    def constrainedBy(bc: Constraint[T]): MultipleResults[T] = new MultipleResults[T](bc, query)

    def withResults(success: Seq[T] => Option[String]): WithoutResults[T] = new WithoutResults[T](All(), query, success)
  }

  sealed abstract class Constraint[T <: DomainObject] {
    def apply(): MongoCursor => MongoCursor
    def and(constraint: Constraint[T]): Constraint[T] = new StackedConstraint[T](this, constraint)
  }

  final class StackedConstraint[T <: DomainObject](constraint1: Constraint[T], constraint2: Constraint[T])
          extends Constraint[T] {
    def apply(): MongoCursor => MongoCursor = mc => constraint2.apply()(constraint1.apply()(mc))
  }

  final case class Limit[T <: DomainObject](n:Int) extends Constraint[T] {
    def apply(): MongoCursor => MongoCursor = mc => mc.limit(n)
  }

  final case class Order[T <: DomainObject, U](first:(FieldType[T, _], SortOrder), rest: (FieldType[T, _], SortOrder)*) extends Constraint[T] {
    def apply(): MongoCursor => MongoCursor = mc => {
      mc.orderBy(orderToSortObject[T](first :: rest.toList))
    }
  }

  final case class All[T <: DomainObject]() extends Constraint[T] {
    def apply(): MongoCursor => MongoCursor = mc => mc.all
  }

  final class MultipleResults[T <: DomainObject : CollectionName : MongoToDomain](constraint: Constraint[T], query: => MongoObject) {
    def withResults(success: Seq[T] => Option[String]): WithoutResults[T] = new WithoutResults[T](constraint, query, success)
  }

  final class WithoutResults[T <: DomainObject : CollectionName : MongoToDomain](constraint: Constraint[T], query: => MongoObject,
                                                                                 success: => (Seq[T]) => Option[String]) {
    def withoutResults(noHits: => Option[String]): UserFunction = mfind[T](query)(constraint.apply())(success)(noHits)
  }

}

