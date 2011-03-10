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

    def one[T <: DomainObject : CollectionName](template:DomainTemplate[T]): UpdateQuery[T] = new UpdateQuery[T](false)

    def many[T <: DomainObject : CollectionName](template:DomainTemplate[T]): UpdateQuery[T] = new UpdateQuery[T](true)
  }

  class UpdateQuery[T <: DomainObject : CollectionName](multiple:Boolean) {
    def where(query: => MongoObject): UpdatedObject[T] = new UpdatedObject[T](multiple, query)
  }

  class UpdatedObject[T <: DomainObject : CollectionName](multiple:Boolean, q: => MongoObject) {
    def withValues(u: => UpdateObject): ExpectWriteResult[T] = new ExpectWriteResult[T](multiple, q, u)
  }

  //def update[T <: DomainObject : CollectionName](q: => MongoObject)(u: => UpdateObject): UserFunction =
  //def safeUpdate[T <: DomainObject : CollectionName](q: => MongoObject)(u: => UpdateObject)(g: MongoWriteResult => Option[String]): UserFunction =
  class ExpectWriteResult[T <: DomainObject : CollectionName](multiple: Boolean, q: => MongoObject, u: => UpdateObject) {

    def expectResults(f: MongoWriteResult => Option[String]): UserFunction = if (multiple) safeUpdateMany[T](q)(u)(f) else safeUpdate[T](q)(u)(f)

    def returnErrors: UserFunction = expectResults(defaultHandler)
  }
}