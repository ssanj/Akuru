/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoCursor
import MongoTypes.MongoObject.sort

trait FindManyDSL { this:MongoFunctions with Tools =>

  final class QueryForMultipleResults[T <: DomainObject : CollectionName : MongoToDomain] {

    import MongoTypes.FieldValueJoiner
    def where(fvj: => FieldValueJoiner[T]): ConstrainedBy[T] = new ConstrainedBy[T](fvj.done)
  }

  final class ConstrainedBy[T <: DomainObject : CollectionName : MongoToDomain](query: => MongoObject) {
    def constrainedBy(bc: Constraint[T]): MultipleResults[T] = new MultipleResults[T](bc, query)

    def withResults(success: Seq[T] => Option[String]): UserFunction = new MultipleResults[T](All(), query).withResults(success)
  }

  sealed abstract class Constraint[T <: DomainObject] {
    def apply(): MongoCursor => MongoCursor
    def and(constraint: Constraint[T]): Constraint[T] = new StackedConstraint[T](this, constraint)
  }

  final class StackedConstraint[T <: DomainObject](constraint1: Constraint[T], constraint2: Constraint[T])
          extends Constraint[T] {
    def apply(): MongoCursor => MongoCursor = mc => constraint2.apply()(constraint1.apply()(mc))
  }

  case class Limit[T <: DomainObject](n:Int) extends Constraint[T] {
    def apply(): MongoCursor => MongoCursor = mc => mc.limit(n)
  }

  case class Order[T <: DomainObject, U](fv:Field[T, U], order:SortOrder) extends Constraint[T] {
    def apply(): MongoCursor => MongoCursor = mc => mc.orderBy(sort(fv, order))
  }

  case class All[T <: DomainObject]() extends Constraint[T] {
    def apply(): MongoCursor => MongoCursor = mc => mc.all
  }

  final class MultipleResults[T <: DomainObject : CollectionName : MongoToDomain](constraint: Constraint[T], query: => MongoObject) {
    def withResults(success: Seq[T] => Option[String]): UserFunction = find[T](query)(constraint.apply())(success)
  }

}

