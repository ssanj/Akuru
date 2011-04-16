/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

final class AkuruMongoWrapperSaveSpec extends AkuruSpecSupport with AkuruMongoWrapper with AkuruMongoWrapperSaveCommon {

  val sampleBlog = singleBlog(0)

  "An AkuruMongoWrapper calling Save" should "call the Failure function when a save fails" in {
    def onSuccess: WorkResult[Unit] = fail("Success should not be called on a Failure")
    def onFailure(wr:MongoWriteResult): WorkResult[Unit] = { wr should be theSameInstanceAs (InvalidMongoWriteResult); Empty }
    val wu = aSave[Blog, Unit](sampleBlog)(onSuccess)(onFailure(_))
    wu(colProvider(InvalidMongoCollection)) verifySuccess
  }

  it should "call the success function when a save succeeds" in {
    val wu = aSave[Blog, Unit](sampleBlog)(Empty)(_ => fail("Failure called on Success"))
    wu(colProvider(ValidMongoCollection)) verifySuccess
  }

  it should "should not call success or failure functions on an Exception" in {
    def onSuccess: WorkResult[Unit] = fail("Success called on Exception")
    def onFailure(wr:MongoWriteResult): WorkResult[Unit] = fail("Failure called on Exception")
    val wu = aSave[Blog, Unit](sampleBlog)(onSuccess)(onFailure(_))
    wu(colProvider(ExceptionalMongoCollection)) verifyFailure (ExceptionalMongoCollection.errorMessage)
  }
}