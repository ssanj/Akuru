/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru;

import MongoTypes.FieldValueJoiner
import MongoTypes.MongoSortObject
import MongoTypes.UpdateObject
/**
 * modify a Blog where titleField === "blah" using (titleField) updateWith (set()) withUpdated(b => ) onError()
 * modify a Blog where titleField === "blah" using (titleField) updateWith (set()) onError()
 *
 * modify a Blog where titleField === "blah" using (titleField) upsertWith (set()) withUpdated(b => ) onError()
 * modify a Blog where titleField === "blah" using (titleField) upsertWith (set()) onError()
 *
 * remove a Blog where titleField === "blah" using (titleField) withDeleted(b => ) onError()
 * modify a Blog where titleField === "blah" using (titleField) onError()
 *
 * query:MongoObject, sort:MongoObject, remove:Boolean, update:MongoObject, returnNew:Boolean, upsert:Boolean
 */
trait ModifyDSL { this:MongoFunctions with Tools =>

  def modify: SingleDomainObject = new SingleDomainObject

  class SingleDomainObject {
    def a[T <: DomainObject : CollectionName : MongoToDomain : ClassManifest](domain:DomainTemplate[T]): ModifyQuery[T] = new ModifyQuery
  }

  class ModifyQuery[T <: DomainObject : CollectionName : MongoToDomain : ClassManifest] {
    def where(query: => FieldValueJoiner[T]): ModifySort[T] = new ModifySort[T](query)
  }


  class ModifySort[T <: DomainObject : CollectionName : MongoToDomain : ClassManifest](query: => FieldValueJoiner[T]) {
    def using[A](sort: => MongoSortObject): ModifyUpdate[T] = new ModifyUpdate[T](query, sort)
  }

  class ModifyUpdate[T <: DomainObject : CollectionName : MongoToDomain : ClassManifest](query: => FieldValueJoiner[T], sort: => MongoSortObject) {
    def updateWith(update: => UpdateObject[T]): WithUpdated[T]= new WithUpdated[T](query, sort, update, false)

    def upsertWith(upsert: => UpdateObject[T]): WithUpserted[T] = new WithUpserted[T](query, sort, upsert, true)
  }

  class WithUpdated[T <: DomainObject : CollectionName : MongoToDomain : ClassManifest](query: => FieldValueJoiner[T],
                                                                                        sort: => MongoSortObject,
                                                                                        update: => UpdateObject[T],
                                                                                        upsert: => Boolean) {
    def withUpdated(wu: T => Option[String]): ModifyOnError[T] = new ModifyOnError[T](query, sort, update, upsert, wu)
  }

  class WithUpserted[T <: DomainObject : CollectionName : MongoToDomain : ClassManifest](query: => FieldValueJoiner[T],
                                                                                         sort: => MongoSortObject,
                                                                                         update: => UpdateObject[T],
                                                                                         upsert: => Boolean) {
    def withUpserted(wu: T => Option[String]): ModifyOnError[T] = new ModifyOnError[T](query, sort, update, upsert, wu)
  }

  class ModifyOnError[T <: DomainObject : CollectionName : MongoToDomain : ClassManifest](query: => FieldValueJoiner[T],
                                                                                          sort: => MongoSortObject,
                                                                                          update: => UpdateObject[T],
                                                                                          upsert: => Boolean,
                                                                                          withUpdated: T => Option[String]) {
    def onError(error: => Option[String]): UserFunction = mfindAndModifyAndReturn(query.done)(sort)(upsert)(update)(withUpdated)(error)
  }

}