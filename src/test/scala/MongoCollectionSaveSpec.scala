/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */


package akuru

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import MongoTypes._
import MongoObject.empty

final class MongoCollectionSaveSpec extends FlatSpec with ShouldMatchers with DomainObjects with MongoFunctions with DomainSupport {

  def success: Option[String] = None

  "A MongoCollection" should "save a new MongoObject" in {
     ({ onDatabase("akuru_test") ~~>
          (drop[Blog] _) ~~>
          ( findOne("title" -> "blah") { t:Blog => fail("Shouldn't have found Blog") } { ignoreError } _) ~~>
          ( save(() => Blog(title = "blah", labels = Seq("test", "random"))) _) ~~>
          ( findOne("title" -> "blah") { t:Blog =>
              t.title should equal ("blah")
              t.labels should equal (Seq("test", "random"))
              success
          } { () => fail("Didn't find Blog") } _)
      } ~~>()) verify
  }

  it should ("handle errors on object creation") in {

    val expectedError = "ExceptionalBlog threw an Exception on Blog creation!Whoops"

    def exceptionBlog():Blog = throw new RuntimeException(expectedError)

    ({ onDatabase("akuru_test") ~~> save(exceptionBlog) _} ~~>()).verifyError(s => s should include regex (expectedError))
  }

  it should ("handle errors that occur during function execution") in {

    ({ onDatabase("akuru_test") ~~> save(() => Person(name = "sanj")) _} ~~>()).verifyError(s => s should include regex (Person.expectedError))
  }

  case class Person(override val id:Option[MongoObjectId] = None, name:String) extends DomainObject

  implicit def personToMongo(p:Person): MongoObject = empty

  implicit object Person extends CollectionName[Person] {

    lazy val expectedError = "no person collection here!"

    lazy val name = throw new RuntimeException("no person collection here!")
  }

  case class VerifyResult(op:Option[String]) {
    def verify() {
      op match {
        case Some(error) => fail(error)
        case None =>
      }
    }

    def verifyError(f:String => Unit) {
      op match {
        case Some(error) => f(error)
        case None => fail("Expected an error")
      }
    }
  }

  implicit def opToVerifyResult(op:Option[String]): VerifyResult = VerifyResult(op)

}