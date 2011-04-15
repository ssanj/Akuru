/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru
package dsl

trait AkuruSave { this:AkuruMongoWrapper =>

  def save: SaveWhat = new SaveWhat

  class SaveWhat {
    def a[T <: DomainObject : CollectionName : DomainTemplateToDBName : DomainToMongo](template: => DomainTemplate[T]): WithValues[T] = {
      implicit val dbName:DBName[T] = implicitly[DomainTemplateToDBName[T]].apply(template)
      new WithValues[T]
    }

    def *[T <: DomainObject : CollectionName : DomainTemplateToDBName : DomainToMongo](template: => DomainTemplate[T]): WithMultipleValues[T] = {
      implicit val dbName:DBName[T] = implicitly[DomainTemplateToDBName[T]].apply(template)
      new WithMultipleValues[T]
    }
  }

  class WithValues[T <: DomainObject : CollectionName : DBName : DomainToMongo] {
    def withValues(dom:T): WithResults[T] = new WithResults[T](dom)
  }

  class WithMultipleValues[T <: DomainObject : CollectionName : DBName : DomainToMongo] {
    def withValues(doms:Seq[T]): WithMutipleResults[T] = new WithMutipleResults[T](doms)
  }

  class WithResults[T <: DomainObject : CollectionName : DBName : DomainToMongo](dom:T) {
    def withResults[R](success: => WorkResult[R]): WithoutResults[T, R] = new WithoutResults[T, R](dom, success)
  }

  class WithMutipleResults[T <: DomainObject : CollectionName : DBName : DomainToMongo](doms:Seq[T]) {
    def withResults[R](success: => WorkResult[R]): WithoutMultipleResults[T, R] = new WithoutMultipleResults[T, R](doms, success)
  }

  class WithoutResults[T <: DomainObject : CollectionName : DBName : DomainToMongo, R](dom:T, success: => WorkResult[R]) {
    def withoutResults(failure: MongoWriteResult => WorkResult[R]): WorkUnit[T, R] = aSave[T, R](dom)(success)(failure)
  }

  class WithoutMultipleResults[T <: DomainObject : CollectionName : DBName : DomainToMongo, R](doms:Seq[T], success: => WorkResult[R]) {
    def withoutResults(failure: (T, MongoWriteResult) => WorkResult[R]): WorkUnit[T, R] =  aSaveMany[T, R](doms)(success)(failure)
  }
}