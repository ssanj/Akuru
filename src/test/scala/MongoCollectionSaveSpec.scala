/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */


package akuru

final class MongoCollectionSaveSpec extends AkuruDSL with CommonSpec {

  import Blog._

  "A MongoCollection" should "save a new MongoObject" in {
     ( onTestDB ~~>
          drop[Blog] ~~>
          ( find * Blog where (titleField("blah")) withResults (_ => fail("Shouldn't have found Blog")) withoutResults success ) ~~>
          save(Blog(titleField("blah"), labelsField(Seq("test", "random")))) ~~>
          ( find * Blog where (titleField("blah")) withResults { blogs =>
              blogs.size should equal (1)
              blogs(0).title.value should equal ("blah")
              blogs(0).labels.value should equal (Seq("test", "random"))
              success
          } withoutResults error("Didn't find a Blog")
      ) ~~>()) verifySuccess
  }

  it should ("handle errors on object creation") in {

    val expectedError = "ExceptionalBlog threw an Exception on Blog creation!Whoops"

    def exceptionBlog():Blog = throw new RuntimeException(expectedError)
    (onTestDB ~~> (save(exceptionBlog)) ~~>()) verifyError has (expectedError)
  }

  it should ("handle errors that occur during function execution") in {
    import Person._
    (onTestDB ~~> save(Person(nameField("sanj"))) ~~>()) verifyError has (Person.expectedError)
  }
}