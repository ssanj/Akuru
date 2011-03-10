/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import akuru.MongoTypes._
import akuru.MongoTypes.MongoObject._

final class MongoCollectionUpdateSingleSpec extends AkuruDSL with CommonSpec  {

  import Blog._
  import Book._
  "A MongoCollection with Updates" should "update an existing value" in {
    ( onTestDB ~~>
            drop[Blog] ~~>
            ( update one Blog where (titleField("Blog updates")) withValues (set(titleField("Phantom updates")))
                    expectResults {wr =>
                      runSafelyWithOptionReturnError {
                        wr.updatedExisting should equal (false)
                        wr.getN should equal (Some(0))
                        wr.ok should equal (true)
                    }}
            ) ~~>
            save(Blog(titleField("Blog updates"), labelsField(Seq("blog, update")))) ~~>
            ( find one Blog where (titleField("Blog updates")) withResults  (b => ignoreSuccess)
                    onError (throw new RuntimeException("Could not find Blog")) ) ~~>
            ( update one Blog where (titleField("Blog updates")) withValues
                    (set(titleField("Blog Updatees")) and set(labelsField(Seq("bl%%g")).splat)) returnErrors ) ~~>
            ( find one Blog where (titleField("Blog updates")) withResults (b => throw new RuntimeException("found old Blog")) onError (noOp) )  ~~>
            ( find one Blog where (titleField("Blog Updatees")) withResults { b:Blog =>
              b.title.value should equal ("Blog Updatees")
              b.labels.value should equal (Seq("bl%%g"))
              success
            } onError (throw new RuntimeException("Could not find updated Blog")) ) ~~>
            drop[Book] ~~>
            save(Book(name = nameField("Programming in Scala"),
                      authors = authorsField(Seq("Martin Odersky", "Lex Spoon", "Bill Venners")),
                      publisher = publisherField("artima"),
                      printVersion = printVersionField(2),
                      price = priceField(54.95D))
            ) ~~>
            ( find one Book where (nameField("Programming in Scala")) withResults { b =>
                b.name.value should equal ("Programming in Scala")
                b.authors.value should equal (Seq("Martin Odersky", "Lex Spoon", "Bill Venners"))
                b.publisher.value should  equal ("artima")
                b.printVersion.value should equal (2)
                b.price.value  should equal (54.95D)
                success
            } onError (throw new RuntimeException("Could not find Book")) ) ~~>
            ( update one Book where (publisherField("artima") and printVersionField(2) and priceField(54.95D))
                      withValues (set(nameField("PISC") and printVersionField(3) and priceField(99.99D))) returnErrors ) ~~>
            ( find one Blog where (nameField("Programming in Scala")) withResults (b => throw new RuntimeException("Found old Book"))
                    onError (noOp) ) ~~>
            ( find one Book where (nameField("PISC") and printVersionField(3)) withResults {b =>
              b.name.value should equal ("PISC")
              b.authors.value should equal (Seq("Martin Odersky", "Lex Spoon", "Bill Venners"))
              b.publisher.value should  equal ("artima")
              b.printVersion.value should equal (3)
              b.price.value  should equal (99.99D)
              success
            } onError (throw new RuntimeException("Could not find Book"))) ~~>()) verifySuccess
  }
}