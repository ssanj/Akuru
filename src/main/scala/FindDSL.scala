/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

trait FindDSL extends FindOneDSL with FindManyDSL { this:MongoFunctions with Tools =>

  def find: HowMany = new HowMany

  final class HowMany {

    def one[T <: DomainObject : CollectionName : MongoToDomain](template: => DomainTemplate[T]): QueryForSingleResult[T] = new QueryForSingleResult[T]

    def many[T <: DomainObject : CollectionName : MongoToDomain](template: => DomainTemplate[T]): QueryForMultipleResults[T]  =
      new QueryForMultipleResults[T]
  }
}