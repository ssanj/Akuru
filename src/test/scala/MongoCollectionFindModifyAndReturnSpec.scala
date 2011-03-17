/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

final class MongoCollectionFindModifyAndReturnSpec extends CommonSpec with ModifyDSL {

  import Blog._
  import MongoTypes.MongoObject._
  "A MongoCollection with findAndModify" should "find and modify an existing object" in {
    ( initBlog ~~>
            save(Blog(titleField("Parry Hotter"), labelsField(Seq("book", "movie")))) ~~>
            ( modify a Blog where titleField === "Parry Hotter" using noSort
                    updateWith Blog(titleField("Harry Potter"), labelsField(Seq("books", "movies")))
                    withUpdated { b =>
                      b.title.value should equal ("Harry Potter")
                      b.labels.value should equal (Seq("books", "movies"))
                      success }
                    onError error("Parry Hotter was not updated!!")
            ) ~~>
            ( modify a Blog where titleField === "Harry Potter" using noSort updateWith set(titleField === "Rahhy Ropper") withUpdated { b =>
              b.title.value should equal ("Rahhy Ropper") //only title has changed
              b.labels.value should equal (Seq("books", "movies")) //has not changed
              success
            } onError error("Harry Potter was not updated!!") ) ~~>()
    ) verifySuccess
  }
}