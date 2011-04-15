/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoCollection
import org.scalatest.mock.MockitoSugar

final class AkuruMongoWrapperSpec extends AkuruSpecSupport with AkuruMongoWrapper with MockitoSugar {

  import Blog._
  "An AkuruMongoWrapper" should "whatever" in {
    def onSuccess: WorkResult[Unit] = fail("Success should not be called on a Failure")

    def onFailure(blog:Blog, wr:MongoWriteResult): WorkResult[Unit] = {
     blog.title.value should equal ("blah")
     Empty
    }

    val blogs:Seq[Blog] = Seq(Blog(titleField === "blah"))

    val wu = aSaveMany[Blog, Unit](blogs)(onSuccess)(onFailure(_, _))
    val result:WorkResult[Unit] = wu(colProvider(new InvalidMongoCollection))
    result.isRight should equal (true)
  }

  implicit val dbName:DBName[Blog] = new DBName[Blog] { val name = "" }

  def colProvider(f: => MongoCollection)(dbName:String, colName:String): MongoCollection = f

  class InvalidMongoCollection extends MongoCollection(null) {
      override def aSave[T <: DomainObject : DomainToMongo](value: => T): Either[String, MongoWriteResult] = Right(new InvalidMongoWriteResult)
  }

  class InvalidMongoWriteResult extends MongoWriteResult(null) { override def ok: Boolean = false }
}