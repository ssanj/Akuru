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
  }

  class WithValues[T <: DomainObject : CollectionName : DBName : DomainToMongo] {
    def withValues(dom:T): WithResults[T] = new WithResults[T](dom)
  }

  class WithResults[T <: DomainObject : CollectionName : DBName : DomainToMongo](dom:T) {
    def withResults[R](success: => WorkResult[R]): WithoutResults[T, R] = new WithoutResults[T, R](dom, success)
  }

  class WithoutResults[T <: DomainObject : CollectionName : DBName : DomainToMongo, R](dom:T, success: => WorkResult[R]) {
    def withoutResults(failure: MongoWriteResult => WorkResult[R]): WorkUnit[T, R] = aSave[T, R](dom)(success)(failure)
  }
}