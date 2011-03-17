/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru;

final class MongoCollectionModifyUpsertSpec extends CommonSpec with ModifyDSL with DSLTools {

  import Blog._
  "A MongoCollection with Modify and Upsert" should "find and modify an existing object" in {
    ( initBlog ~~>
            ( modify a Blog where titleField === "Functor" sortBy (titleField -> ASC)
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