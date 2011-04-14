/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package dsl

import MongoTypes.UpdateObject
import MongoTypes.Query

/**
 * update a Blog where (titleField === "lessons learned" and labelsField === Seq("misc"))
 *  withValues ($$set(titleField === "Lessons Learned")) expectResults(_) ~~>
 * update a Blog where titleField === "lessons learned" withValues (new Blog(..)) expectResults(_) ~~>
 * update * Blog where (titleField("lessons learned")) withValues ($$set(titleField("Lessons Learned"))) returnErrors ~~>
 */
trait UpdateDSL { this:MongoFunctions with Tools =>

  def update: HowMany = new HowMany

  def upsert: InsertOne = new InsertOne

  class InsertOne {
    def a[T <: DomainObject : CollectionName : ClassManifest](template:DomainTemplate[T]): UpdateQuery[T] = new UpdateQuery[T](multiple = false,
      upsert = true)
  }

  class HowMany {

    def a[T <: DomainObject : CollectionName : ClassManifest](template:DomainTemplate[T]): UpdateQuery[T] = new UpdateQuery[T](multiple = false,
      upsert = false)

    def *[T <: DomainObject : CollectionName : ClassManifest](template:DomainTemplate[T]): UpdateQuery[T] = new UpdateQuery[T](multiple = true,
      upsert = false)
  }

  class UpdateQuery[T <: DomainObject : CollectionName : ClassManifest](multiple: => Boolean, upsert: => Boolean) {
    def where(fvj: => Query[T]): UpdatedObject[T] = new UpdatedObject[T](multiple, upsert, fvj)
  }

  class UpdatedObject[T <: DomainObject : CollectionName : ClassManifest](multiple: => Boolean, upsert: => Boolean, q: => Query[T]) {
    def withValues(u: => UpdateObject[T]): ExpectWriteResult[T] = new ExpectWriteResult[T](multiple, upsert, q, u)
  }

  class ExpectWriteResult[T <: DomainObject : CollectionName : ClassManifest](multiple: => Boolean, upsert: => Boolean, q: => Query[T],
                                                                              u: => UpdateObject[T]) {

    def expectResults(f: MongoWriteResult => Option[String]): UserFunction = msafeUpdate[T](q.splat)(u)(f)(multiple)(upsert)

    def returnErrors: UserFunction = expectResults(defaultHandler)
  }
}