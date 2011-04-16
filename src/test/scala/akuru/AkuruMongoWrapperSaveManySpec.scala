/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoCollection

//TODO: Add more scenarios for failure/success/exception combinations
final class AkuruMongoWrapperSaveManySpec extends AkuruSpecSupport with AkuruMongoWrapper with AkuruMongoWrapperSaveCommon {

  "An AkuruMongoWrapper calling SaveMany" should "call the Failure function when a save fails" in {
    def onSuccess: WorkResult[Unit] = fail("Success should not be called on a Failure")

    def onFailure(blog:Blog, wr:MongoWriteResult): WorkResult[Unit] = {
      blog.title.value should equal ("blah")
      wr should be theSameInstanceAs (InvalidMongoWriteResult)
      Empty
    }

    val wu = aSaveMany[Blog, Unit](singleBlog)(onSuccess)(onFailure(_, _))
    wu(colProvider(InvalidMongoCollection)) verifySuccess
  }

  it should "call the success function when a save succeeds" in {
    def onFailure(blog:Blog, wr:MongoWriteResult): WorkResult[Unit] = fail("Expected success, but failed on Blog: " + blog)
    val wu = aSaveMany[Blog, Unit](singleBlog)(Empty)(onFailure(_, _))
    wu(colProvider(ValidMongoCollection)) verifySuccess
  }

  it should "not call the success or failure functions when an Exception is thrown" in {
    def onSuccess: WorkResult[Unit] = fail("Success should not be called on an Exception")
    def onFailure(blog:Blog, wr:MongoWriteResult): WorkResult[Unit] = fail("Failure should not be called on an Exception")

    val wu = aSaveMany[Blog, Unit](singleBlog)(onSuccess)(onFailure(_, _))
    wu(colProvider(ExceptionalMongoCollection)) verifyFailure (ExceptionalMongoCollection.errorMessage)
  }

  it should "call the failure function on the first failure" in {
    def onSuccess: WorkResult[Unit] = fail("Success should not be called on a failure")
    def onFailure(blog:Blog, wr:MongoWriteResult): WorkResult[Unit] = { blog should be theSameInstanceAs (multipleBlogs(1)); Empty }
    val wu = aSaveMany[Blog, Unit](multipleBlogs)(onSuccess)(onFailure(_, _))
    wu(colProvider(new FailOnMongoCollection(multipleBlogs(1)))) verifySuccess
  }

  it should "call the success function if all saves pass" in {
    def onFailure(blog:Blog, wr:MongoWriteResult): WorkResult[Unit] = fail("Expected success but failed on Blog: " + blog)
    val wu = aSaveMany[Blog, Unit](multipleBlogs)(Empty)(onFailure(_, _))
    wu(colProvider(ValidMongoCollection)) verifySuccess
  }

  class FailOnMongoCollection(blog:Blog) extends MongoCollection(null) {
      override def aSave[T <: DomainObject : DomainToMongo](value: => T): Either[String, MongoWriteResult] =
        Right(if (!(blog eq value)) ValidMongoWriteResult else InvalidMongoWriteResult)
  }
}