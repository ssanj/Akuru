/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package runners

import akuru._
import MongoTypes.Query
import MongoTypes.MongoCursor
import akuru.dsl.DSLTools

trait AkuruFinder { this:AkuruMongoWrapper with AkuruFunctions with Tools with DSLTools =>

  def find: AKuruFindMany = new AKuruFindMany

  final class AKuruFindMany {
    def *[T <: DomainObject : CollectionName : MongoToDomain : DomainTemplateToDBName](template: => DomainTemplate[T]): QueryForMultipleResults[T] = {
      implicit val dbName:DBName[T] = implicitly[DomainTemplateToDBName[T]].apply(template)
      new QueryForMultipleResults[T]
    }
  }

  final class QueryForMultipleResults[T <: DomainObject : CollectionName : MongoToDomain : DBName] {
    def where(fvj: => Query[T]): ConstrainedBy[T] = new ConstrainedBy[T](fvj.splat)
  }

  final class ConstrainedBy[T <: DomainObject : CollectionName : MongoToDomain : DBName](query: => MongoObject) {
    def constrainedBy(bc: Constraint[T]): MultipleResults[T] = new MultipleResults[T](bc, query)
    def withResults[R](success: Seq[T] => WorkResult[R]): WithoutResults[T, R] = new WithoutResults[T, R](All(), query, success)
  }

  sealed abstract class Constraint[T <: DomainObject] {
    def apply(): MongoCursor => MongoCursor
    def and(constraint: Constraint[T]): Constraint[T] = new StackedConstraint[T](this, constraint)
  }

  final class StackedConstraint[T <: DomainObject](constraint1: Constraint[T], constraint2: Constraint[T]) extends Constraint[T] {
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

  final class MultipleResults[T <: DomainObject : CollectionName : MongoToDomain : DBName](constraint: Constraint[T], query: => MongoObject) {
    def withResults[R](success: Seq[T] => WorkResult[R]): WithoutResults[T, R] = new WithoutResults[T, R](constraint, query, success)
  }

  final class WithoutResults[T <: DomainObject : CollectionName : MongoToDomain : DBName, R](constraint: Constraint[T], query: => MongoObject,
                                                                                 success: => (Seq[T]) => WorkResult[R]) {
    def withoutResults(noHits: => WorkResult[R]): WorkUnit[T, R] = afind[T, R](query)(constraint.apply())(success)(noHits)
  }
}
