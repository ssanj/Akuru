/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.UpdateObject
import MongoTypes.MongoWriteResult

/**
 * update one Blog where (titleField("lessons learned")) withValues (set(titleField("Lessons Learned"))) expectResults(_) ~~>
 * update many Blog where (titleField("lessons learned")) withValues (set(titleField("Lessons Learned"))) returnErrors ~~>
 */
trait UpdateDSL { this:MongoFunctions with Tools =>

  def update: HowMany = new HowMany

  class HowMany {
    def one[T <: DomainObject : CollectionName](template:DomainTemplate[T]): UpdateQuery[T] = new UpdateQuery[T]
  }

  class UpdateQuery[T <: DomainObject : CollectionName] {
    def where(query: => MongoObject): UpdatedObject[T] = new UpdatedObject[T](query)
  }

  class UpdatedObject[T <: DomainObject : CollectionName](q: => MongoObject) {
    def withValues(u: => UpdateObject): ExpectWriteResult[T] = new ExpectWriteResult[T](q, u)
  }

  //def update[T <: DomainObject : CollectionName](q: => MongoObject)(u: => UpdateObject): UserFunction =
  //def safeUpdate[T <: DomainObject : CollectionName](q: => MongoObject)(u: => UpdateObject)(g: MongoWriteResult => Option[String]): UserFunction =
  class ExpectWriteResult[T <: DomainObject : CollectionName](q: => MongoObject, u: => UpdateObject) {

    def expectResults(f: MongoWriteResult => Option[String]): UserFunction = safeUpdate[T](q)(u)(f)

    def returnErrors: UserFunction = safeUpdate[T](q)(u)(defaultHandler)
  }
}