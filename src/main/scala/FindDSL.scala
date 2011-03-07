/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

trait FindDSL extends MongoFunctions with Tools {

  def find: HowMany = new HowMany

  final class HowMany {

    def one[T <: DomainObject : CollectionName : MongoToDomain](template: => DomainTemplate[T]): Query[T] = new Query[T]

    def many[T <: DomainObject : CollectionName : MongoToDomain](template: => DomainTemplate[T]): Query[T]  = new Query[T]
  }

  final class Query[T <: DomainObject : CollectionName : MongoToDomain] {
    def where(query: => MongoObject): Results[T] = new Results[T](query)
  }

  final class Results[T <: DomainObject : CollectionName : MongoToDomain](query: => MongoObject) {
    def withResults(success: T => Option[String]): Errors[T] = new Errors[T](query, success)
  }

  final class Errors[T <: DomainObject : CollectionName : MongoToDomain](query: => MongoObject, success: T => Option[String]) {
    def onError(error: => Unit): UserFunction = findOne[T](query)(success)(error)
  }
}
