/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

final class MongoCollectionFindModifyAndReturnSpec extends CommonSpec {

  import Blog._
  import MongoTypes.MongoObject._
  "A MongoCollection with findAndModify" should "find and modify an existing object" in {
    (onTestDB ~~>
            drop[Blog] ~~>
            save(Blog(title = titleField("Parry Hotter"), labels = labelsField(Seq("book", "movie")))) ~~>
            findAndModifyAndReturn(titleField("Parry Hotter"))(noSort) {
              Blog(title = titleField("Harry Potter"), labels = labelsField(Seq("books", "movies"))) }{ b:Blog =>
              b.title.value should equal ("Harry Potter")
              b.labels.value should equal (Seq("books", "movies"))
              success
            } { Some("Parry Hotter was not updated!!") } ~~>
            findAndModifyAndReturn(titleField("Harry Potter"))(noSort)( set(titleField("Rahhy Ropper"))) { b: Blog =>
              b.title.value should equal ("Rahhy Ropper") //only title has changed
              b.labels.value should equal (Seq("books", "movies")) //has not changed
              success
            } { Some("Harry Potter was not updated!!") } ~~>()
    ) verifySuccess
  }
}