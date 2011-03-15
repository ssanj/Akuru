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

    def one[T <: DomainObject : CollectionName : ClassManifest](template:DomainTemplate[T]): UpdateQuery[T] = new UpdateQuery[T](false)

    def many[T <: DomainObject : CollectionName : ClassManifest](template:DomainTemplate[T]): UpdateQuery[T] = new UpdateQuery[T](true)
  }

  class UpdateQuery[T <: DomainObject : CollectionName : ClassManifest](multiple:Boolean) {
    import MongoTypes.FieldValueJoiner
    def where(fvj: => FieldValueJoiner[T]): UpdatedObject[T] = new UpdatedObject[T](multiple, fvj.done)
  }

  class UpdatedObject[T <: DomainObject : CollectionName : ClassManifest](multiple: => Boolean, q: => MongoObject) {
    def withValues(u: => UpdateObject[T]): ExpectWriteResult[T] = new ExpectWriteResult[T](multiple, q, u)
  }

  class ExpectWriteResult[T <: DomainObject : CollectionName : ClassManifest](multiple: Boolean, q: => MongoObject, u: => UpdateObject[T]) {

    def expectResults(f: MongoWriteResult => Option[String]): UserFunction = if (multiple) safeUpdateMany[T](q)(u)(f) else safeUpdate[T](q)(u)(f)

    def returnErrors: UserFunction = expectResults(defaultHandler)
  }
}