/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoCollection
import akuru.domain.TestDomainObjects

trait AkuruMongoWrapperSaveCommon { this:TestDomainObjects =>

  import Blog._
  val singleBlog:Seq[Blog] = Seq(Blog(titleField === "blah"))
  val multipleBlogs:Seq[Blog] = Seq(singleBlog(0), Blog(titleField === "misc"), Blog(titleField === "meh"))
  implicit val dbName:DBName[Blog] = new DBName[Blog] { val name = "" }

  object ValidMongoWriteResult extends MongoWriteResult(null) { override def ok: Boolean = true }

  object InvalidMongoWriteResult extends MongoWriteResult(null) { override def ok: Boolean = false }

  object InvalidMongoCollection extends MongoCollection(null) {
      override def aSave[T <: DomainObject : DomainToMongo](value: => T): Either[String, MongoWriteResult] = Right(InvalidMongoWriteResult)
  }

  object ValidMongoCollection extends MongoCollection(null) {
      override def aSave[T <: DomainObject : DomainToMongo](value: => T): Either[String, MongoWriteResult] = Right(ValidMongoWriteResult)
  }

  object ExceptionalMongoCollection extends MongoCollection(null) {
     val errorMessage = "Could not save object due to Exception."
      override def aSave[T <: DomainObject : DomainToMongo](value: => T): Either[String, MongoWriteResult] = Left(errorMessage)
  }
}