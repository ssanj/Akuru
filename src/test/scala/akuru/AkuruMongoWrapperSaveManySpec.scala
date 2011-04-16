/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoCollection

final class AkuruMongoWrapperSaveManySpec extends AkuruSpecSupport with AkuruMongoWrapper {

  import Blog._
  val singleBlog:Seq[Blog] = Seq(Blog(titleField === "blah"))
  val multipleBlogs:Seq[Blog] = Seq(singleBlog(0), Blog(titleField === "misc"), Blog(titleField === "meh"))
  implicit val dbName:DBName[Blog] = new DBName[Blog] { val name = "" }

  def colProvider(f: => MongoCollection)(dbName:String, colName:String): MongoCollection = f

  "An AkuruMongoWrapper calling SaveMany" should "call the Failure function when a save fails" in {
    def onSuccess: WorkResult[Unit] = fail("Success should not be called on a Failure")
    def onFailure(blog:Blog, wr:MongoWriteResult): WorkResult[Unit] = { blog.title.value should equal ("blah"); Empty }
    val wu = aSaveMany[Blog, Unit](singleBlog)(onSuccess)(onFailure(_, _))
    wu(colProvider(InvalidMongoCollection)) verifySuccess
  }

  it should "call the success function when a save succeeds" in {
    def onSuccess: WorkResult[Unit] = Empty
    def onFailure(blog:Blog, wr:MongoWriteResult): WorkResult[Unit] = fail("Expected success, but failed on Blog: " + blog)
    val wu = aSaveMany[Blog, Unit](singleBlog)(onSuccess)(onFailure(_, _))
    wu(colProvider(ValidMongoCollection)) verifySuccess
  }

  it should "return handle Exceptions thrown when saving" in {
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
    def onSuccess: WorkResult[Unit] = Empty
    def onFailure(blog:Blog, wr:MongoWriteResult): WorkResult[Unit] = fail("Expected success but failed on Blog: " + blog)
    val wu = aSaveMany[Blog, Unit](multipleBlogs)(onSuccess)(onFailure(_, _))
    wu(colProvider(ValidMongoCollection)) verifySuccess
  }

  class ValidMongoWriteResult extends MongoWriteResult(null) { override def ok: Boolean = true }

  class InvalidMongoWriteResult extends MongoWriteResult(null) { override def ok: Boolean = false }

  object InvalidMongoCollection extends MongoCollection(null) {
      override def aSave[T <: DomainObject : DomainToMongo](value: => T): Either[String, MongoWriteResult] = Right(new InvalidMongoWriteResult)
  }

  object ValidMongoCollection extends MongoCollection(null) {
      override def aSave[T <: DomainObject : DomainToMongo](value: => T): Either[String, MongoWriteResult] = Right(new ValidMongoWriteResult)
  }

  object ExceptionalMongoCollection extends MongoCollection(null) {
     val errorMessage = "Could not save object due to Exception."
      override def aSave[T <: DomainObject : DomainToMongo](value: => T): Either[String, MongoWriteResult] = Left(errorMessage)
  }

  class FailOnMongoCollection(blog:Blog) extends MongoCollection(null) {
      override def aSave[T <: DomainObject : DomainToMongo](value: => T): Either[String, MongoWriteResult] =
        Right(if (!(blog eq value)) new ValidMongoWriteResult else new InvalidMongoWriteResult)
  }
}