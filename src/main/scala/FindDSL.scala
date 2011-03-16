/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

trait FindDSL extends FindManyDSL { this:MongoFunctions with Tools =>

  def find[T <: DomainObject : CollectionName : MongoToDomain](template: => DomainTemplate[T]): QueryForMultipleResults[T]  =
      new QueryForMultipleResults[T]
}