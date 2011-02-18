/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

final class MongoCollectionFindAndModifySpec extends CommonSpec {

  import Blog._
  import MongoTypes.MongoObject.empty
  "A MongoCollection with findAndModify" should "find and modify an existing object" in {
    (onTestDB ~~>
            drop[Blog] ~~>
            save(Blog(title = titleField("Parry Hotter"), labels = labelsField(Seq("book", "movie")))) ~~>
            findAndModifyAndReturn(Blog.titleField("Parry Hotter"))(empty) {
              Blog(title = titleField("Harry Potter"), labels = labelsField(Seq("books", "movies"))) }{ b:Blog =>
              b.title.value should equal ("Harry Potter")
              b.labels.value should equal (Seq("books", "movies"))
              success
            } {
              Some("Parry Hotter was not updated!!")
            } ~~>()
    ) verifySuccess
  }
}