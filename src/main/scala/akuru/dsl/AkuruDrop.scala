/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru
package dsl


/**
 * A DSL for dropping a collection.
 *
 * @usecase drop collection (DomainObject) withResults(...)
 *
 * @note This DSL is incongruous with other DSLs which have a matching withResults and withoutResults methods. The reason for this is that the
 * <code>MongoCollection#drop</code> method returns <code>Unit</code>. As such there is nothing to base the withoutResults condition on.
 * <code>MongoCollection#drop</code> will either succeed in which case <code>withResults</code> will be called or fail with an <code>Exception</code>
 * in which case a Left(errorString) will be returned. Hence there is no need for a <code>withoutResults</code> method.
 */
trait AkuruDrop { this:AkuruMongoWrapper =>

  def drop = new {
    def collection[T <: DomainObject : CollectionName : DomainTemplateToDBName](template: => DomainTemplate[T]): WithResult[T] = {
      implicit val dbName:DBName[T] = implicitly[DomainTemplateToDBName[T]].apply(template)
      new WithResult[T]
    }
  }

  class WithResult[T <: DomainObject : CollectionName : DBName] {
    def withResults[R](success: => WorkResult[R]): WorkUnit[T, R] = {
      aDrop[T, R](success)
    }
  }
}