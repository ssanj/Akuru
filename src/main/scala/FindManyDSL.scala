/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoCursor
import MongoTypes.MongoObject.sort
import MongoTypes.SortObjectJoiner

trait FindManyDSL { this:MongoFunctions with Tools =>

  final class QueryForMultipleResults[T <: DomainObject : CollectionName : MongoToDomain] {

    import MongoTypes.FieldValueJoiner
    def where(fvj: => FieldValueJoiner[T]): ConstrainedBy[T] = new ConstrainedBy[T](fvj.done)
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

  final class MergedConstraint[T <: DomainObject](soj:SortObjectJoiner, f: MongoCursor => SortObjectJoiner => MongoCursor)
          extends Constraint[T] {
    def apply(): MongoCursor => MongoCursor = mc => f(mc)(soj)
    def +[V](other: Order[T, V]): MergedConstraint[T] = new MergedConstraint[T](soj and (sort(other.fv, other.order)) ,
      mc => soj => mc.orderBy(soj))
  }

  case class Limit[T <: DomainObject](n:Int) extends Constraint[T] {
    def apply(): MongoCursor => MongoCursor = mc => mc.limit(n)
  }

  case class Order[T <: DomainObject, U](fv:Field[T, U], order:SortOrder) extends Constraint[T] {
    def apply(): MongoCursor => MongoCursor = mc => mc.orderBy(sort(fv, order))
    def +[V](other: Order[T, V]): MergedConstraint[T] = new MergedConstraint[T](sort(fv, order) and sort(other.fv, other.order),
      mc => soj => mc.orderBy(soj))
  }

  case class All[T <: DomainObject]() extends Constraint[T] {
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

