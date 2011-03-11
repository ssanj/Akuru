/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import akuru.MongoTypes.MongoObject._

final class MongoCollectionUpdateSingleSpec extends AkuruDSL with CommonSpec with MongoCollectionCommonUpdateSpec {

  import Blog._
  import Book._

  override def cardinality: UpdateQuery[Blog] = ( update one Blog )
  override def specName:String = "A MongoCollection with Single Updates"
  override def expectedResults = 1

  it should "update a single matching DomainObject" in {
    ( initBlog ~~>
            save(Blog(titleField === "Blog updates", labelsField === Seq("blog, update"))) ~~>
            ( find one Blog where (titleField === "Blog updates") withResults  (b => ignoreSuccess)
                    onError (ex("Could not find Blog")) ) ~~>
            ( update one Blog where (titleField === "Blog updates") withValues
                    (set(titleField === "Blog Updatees") & set(labelsField === Seq("bl%%g", "sm%%g"))) returnErrors ) ~~>
            ( find one Blog where (titleField === "Blog updates") withResults (b => ex("found old Blog")) onError (noOp) )  ~~>
            ( find one Blog where (titleField === "Blog Updatees") withResults { b:Blog =>
              b.title.value should equal ("Blog Updatees")
              b.labels.value should equal (Seq("bl%%g", "sm%%g"))
              success
            } onError (ex("Could not find updated Blog")) )
     ) ~~>() verifySuccess
  }

  it should "update multiple fields on matching DomainObjects" in {
    ( initBook ~~>
            save(Book(nameField === "Programming in Scala",
                      authorsField === Seq("Martin Odersky", "Lex Spoon", "Bill Venners"),
                      publisherField === "artima",
                      printVersionField === 2,
                      priceField === 54.95D)
            ) ~~>
            ( find one Book where (nameField === "Programming in Scala") withResults { b =>
                b.name.value should equal ("Programming in Scala")
                b.authors.value should equal (Seq("Martin Odersky", "Lex Spoon", "Bill Venners"))
                b.publisher.value should  equal ("artima")
                b.printVersion.value should equal (2)
                b.price.value  should equal (54.95D)
                success
            } onError (ex("Could not find Book")) ) ~~>
            ( update one Book where (publisherField === "artima" and printVersionField === 2 and priceField === 54.95D)
                      withValues (set(nameField === "PISC" & printVersionField === 3 & priceField === 99.99D)) returnErrors ) ~~>
            ( find one Blog where (nameField === "Programming in Scala") withResults (b => ex("Found old Book"))
                    onError (noOp) ) ~~>
            ( find one Book where (nameField === "PISC" and printVersionField === 3) withResults {b =>
              b.name.value should equal ("PISC")
              b.authors.value should equal (Seq("Martin Odersky", "Lex Spoon", "Bill Venners"))
              b.publisher.value should  equal ("artima")
              b.printVersion.value should equal (3)
              b.price.value should equal (99.99D)
              success
            } onError (ex("Could not find Book")) )
    ) ~~>() verifySuccess
  }
}