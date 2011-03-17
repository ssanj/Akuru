/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

final class MongoCollectionModifyWithoutSortingSpec extends CommonSpec with ModifyDSL with DSLTools {

  import Blog._
  import MongoTypes.MongoObject._
  "A MongoCollection with modify and no sorting" should "find and modify an existing object" in {
    ( initBlog ~~>
            save(Blog(titleField("Parry Hotter"), labelsField(Seq("book", "movie")))) ~~>
            ( modify a Blog where titleField === "Parry Hotter"  updateWith Blog(titleField("Harry Potter"), labelsField(Seq("books", "movies")))
                    withUpdated { b =>
                      b.title.value should equal ("Harry Potter")
                      b.labels.value should equal (Seq("books", "movies"))
                      success }
                    onError error("Parry Hotter was not updated!!")
            ) ~~>
            ( modify a Blog where titleField === "Harry Potter" updateWith set(titleField === "Rahhy Ropper")
                    withUpdated { b =>
                      b.title.value should equal ("Rahhy Ropper") //only title has changed
                      b.labels.value should equal (Seq("books", "movies")) //has not changed
                      success
                    }
              onError error("Harry Potter was not updated!!") )
    ) ~~>() verifySuccess
  }

  it should "upsert a Ddomain object that does not exist" in  {
    ( initBlog ~~>
        ( modify a Blog where titleField === "The Two Towers" upsertWith Blog(titleField === "The Two Towers")
                withUpserted {b =>
                  b.title.value should equal ("The Two Towers")
                  success
                }
          onError error("Could not find the Two Towers") )
    ) ~~>() verifySuccess
  }
}