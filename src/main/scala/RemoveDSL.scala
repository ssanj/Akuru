/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru;

import MongoTypes.FieldValueJoiner
import MongoTypes.MongoSortObject
import MongoTypes.UpdateObject
/**
 * remove a Blog where titleField === "blah" sortBy (titleField -> DSC) withDeleted(b => ) onError()
 * remove a Blog where titleField === "blah" withDeleted(b => ) onError()
 * remove a Blog where titleField === "blah" sortBy (titleField -> ASC) onError()
 * remove a Blog where titleField === "blah" onError()
 */

trait RemoveDSL { this: MongoFunctions with Tools with DSLTools =>

  def remove: SingleDomainObject = new SingleDomainObject

  final class SingleDomainObject {
    def a[T <: DomainObject : CollectionName : MongoToDomain : ClassManifest](domain:DomainTemplate[T]): RemoveQuery[T] = new RemoveQuery[T]
  }

  final class RemoveQuery[T <: DomainObject : CollectionName : MongoToDomain : ClassManifest] {
    def where(query: => FieldValueJoiner[T]): RemoveSort[T] = new RemoveSort[T](query)
  }

  sealed abstract class BaseWithDeleted[T <: DomainObject : CollectionName : MongoToDomain : ClassManifest](query: => FieldValueJoiner[T],
                                                                                                     sort: => MongoSortObject) {
    def withDeleted(wd: T => Option[String]): RemoveOnError[T] = new RemoveOnError[T](query, sort, wd)

    def onError(error: => Option[String]): UserFunction = new RemoveOnError[T](query, sort, _ => None).onError(error)
  }

  final class RemoveSort[T <: DomainObject : CollectionName : MongoToDomain : ClassManifest](query: => FieldValueJoiner[T])
          extends BaseWithDeleted[T](query, noSorting) {
    def sortBy(first:(Field[T, _], SortOrder), rest: (Field[T, _], SortOrder)*): WithDeleted[T] =
      new WithDeleted[T](query, orderToSortObject[T](first :: rest.toList))
  }

  final class WithDeleted[T <: DomainObject : CollectionName : MongoToDomain : ClassManifest](query: => FieldValueJoiner[T],
      sort: => MongoSortObject) extends BaseWithDeleted[T](query, sort)

  final class RemoveOnError[T <: DomainObject : CollectionName : MongoToDomain : ClassManifest](query: => FieldValueJoiner[T],
                                                                                                sort: => MongoSortObject,
                                                                                                withDeleted: T => Option[String]) {
    def onError(error: => Option[String]): UserFunction = mfindAndModifyAndRemove(query.done)(sort)(withDeleted)(error)
  }

}