/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */


package akuru

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

final class MongoCollectionSaveSpec extends CommonSpec {

  import Blog._

  "A MongoCollection" should "save a new MongoObject" in {
     ({ onTestDB ~~>
          drop[Blog] ~~>
          findOne[Blog](titleField("blah")) { _ => fail("Shouldn't have found Blog") } { noOp } ~~>
          save(Blog(title = titleField("blah"), labels = labelsField(Seq("test", "random")))) ~~>
          findOne[Blog](titleField("blah")) { t =>
              t.title.value should equal ("blah")
              t.labels.value should equal (Seq("test", "random"))
              success
          } { fail("Didn't find Blog") }
      } ~~>()) verifySuccess
  }

  it should ("handle errors on object creation") in {

    val expectedError = "ExceptionalBlog threw an Exception on Blog creation!Whoops"

    def exceptionBlog():Blog = throw new RuntimeException(expectedError)
    (onTestDB ~~> (save(exceptionBlog)) ~~>()) verifyError has (expectedError)
  }

  it should ("handle errors that occur during function execution") in {
    import Person._
    (onTestDB ~~> save(Person(name = nameField("sanj"))) ~~>()) verifyError has (Person.expectedError)
  }
}