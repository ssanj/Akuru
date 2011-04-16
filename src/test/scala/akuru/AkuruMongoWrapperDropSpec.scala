/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoCollection

final class AkuruMongoWrapperDropSpec extends AkuruSpecSupport with AkuruMongoWrapper {

  implicit val dbName:DBName[Blog] = new DBName[Blog] { val name = "" }

  def colProvider(f: => MongoCollection)(dbName:String, colName:String): MongoCollection = f

  "An AkuruMongoWrapper calling drop" should "call success if the drop was successful" in {
    def onSuccess: WorkResult[Unit] = Empty
    val wu = aDrop[Blog, Unit](onSuccess)
    wu(colProvider(ValidMongoCollection)) verifySuccess
  }

  it should "not call the success function on an Exception" in {
    def onSuccess: WorkResult[Unit] = fail("Success should not be called on an Exception")
    val wu = aDrop[Blog, Unit](onSuccess)
    wu(colProvider(InvalidMongoCollection)) verifyFailure (InvalidMongoCollection.errorMessage)
  }

  object ValidMongoCollection extends MongoCollection(null) { override def aDrop: Either[String, Unit] = Right{} }

  object InvalidMongoCollection extends MongoCollection(null) {
    val errorMessage = "There was an Exception"
    override def aDrop: Either[String, Unit] = Left("There was an Exception")
  }
}