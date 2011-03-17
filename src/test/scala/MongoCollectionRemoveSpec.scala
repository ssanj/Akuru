/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

final class MongoCollectionRemoveSpec extends CommonSpec with AkuruDSL {

  import Blog._
  "A MongoCollection with remove" should "not find or remove a non-existant object" in {
    ( initBlog ~~>
            ( remove a Blog where titleField === "Storms" sortBy (titleField -> ASC) withDeleted (b => Some("Found an non-existant blog " + b))
            onError error("Could not delete Blog"))
    ) ~~>() verifyError (_ should equal ("Could not delete Blog"))
  }

  it should "find and remove an existing object" in {
    ( initBlog ~~>
            save(Blog(titleField("Storms"), labelsField(Seq("qld", "weather")))) ~~>
            ( find many Blog where titleField === "Storms" withResults {b => success} withoutResults error("could not find Blog")) ~~>
            ( remove a Blog where titleField === "Storms" withDeleted {b =>
              b.title.value should equal ("Storms")
              b.labels.value should equal (Seq("qld", "weather"))
              success
            } onError error("Could not remove Blog")) ~~>
            ( find many Blog where titleField === "Storms" withResults {b => Some("Returned deleted Blog " + b)} withoutResults success )
      ) ~~>() verifySuccess
  }
}