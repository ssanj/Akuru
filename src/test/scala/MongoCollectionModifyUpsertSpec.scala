/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru;

final class MongoCollectionModifyUpsertSpec extends CommonSpec with ModifyDSL {

  import Blog._
  import MongoTypes.MongoObject._
  "A MongoCollection with findAndModify" should "find and modify an existing object" in {
    ( initBlog ~~>
            ( modify a Blog where titleField === "Functor" using sort(titleField, ASC)
                    upsertWith Blog(titleField("Functor"), labelsField(Seq("fp", "programming")))
                    withUpserted { b =>
                      b.title.value should equal ("Functor")
                      b.labels.value should equal (Seq("fp", "programming"))
                      success
                    } onError error("Functor Blog was not upserted!!")
            )
    ) ~~>() verifySuccess
  }
}