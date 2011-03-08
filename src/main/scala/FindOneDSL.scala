/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

trait FindOneDSL { this:MongoFunctions with Tools =>

  final class QueryForSingleResult[T <: DomainObject : CollectionName : MongoToDomain] {
    def where(query: => MongoObject): Results[T] = new Results[T](query)
  }

  final class Results[T <: DomainObject : CollectionName : MongoToDomain](query: => MongoObject) {
    def withResults(success: T => Option[String]): ErrorsFindOne[T] = new ErrorsFindOne[T](query, success)
  }

  final class ErrorsFindOne[T <: DomainObject : CollectionName : MongoToDomain](query: => MongoObject, success: T => Option[String]) {
    def onError(error: => Unit): UserFunction = findOne[T](query)(success)(error)
  }
}
