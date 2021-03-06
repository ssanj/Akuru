/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package dsl

/**
 * find * X where (field1 === value1 and field2 === value2) withResults (b => doSomething) withoutResults (handleError)
 * find a X where (field1 === value1) constrainedBy (Limit(N) and Order(field1 -> ASC)) withResults {b => doSomething)  withoutResults (handleError)
 */
trait FindDSL extends FindManyDSL { this:MongoFunctions with Tools with DSLTools =>

  def find: FindMany = new FindMany

  final class FindMany {
    def *[T <: DomainObject : CollectionName : MongoToDomain](template: => DomainTemplate[T]): QueryForMultipleResults[T]  =
            new QueryForMultipleResults[T]
  }
}