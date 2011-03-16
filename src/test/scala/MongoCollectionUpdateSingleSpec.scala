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
            ( find (Blog) where (titleField === "Blog updates") withResults  (b => ignoreSuccess)
                    withoutResults error("Could not find Blog") ) ~~>
            ( update one Blog where (titleField === "Blog updates") withValues
                    (set(titleField === "Blog Updatees" & labelsField === Seq("bl%%g", "sm%%g"))) returnErrors ) ~~>
            ( find (Blog) where (titleField === "Blog updates") withResults (b => ex("found old Blog")) withoutResults success )  ~~>
            ( find (Blog) where (titleField === "Blog Updatees") withResults { blogs =>
              blogs.size should equal (1)
              val b = blogs(0)
              b.title.value should equal ("Blog Updatees")
              b.labels.value should equal (Seq("bl%%g", "sm%%g"))
              success
            } withoutResults error("Could not find updated Blog") )
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
            ( find (Book) where (nameField === "Programming in Scala") withResults { books =>
                books.size should equal (1)
                val b = books(0)
                b.name.value should equal ("Programming in Scala")
                b.authors.value should equal (Seq("Martin Odersky", "Lex Spoon", "Bill Venners"))
                b.publisher.value should  equal ("artima")
                b.printVersion.value should equal (2)
                b.price.value  should equal (54.95D)
                success
            } withoutResults error("Could not find Book") ) ~~>
            ( update one Book where (publisherField === "artima" and2 printVersionField === 2 and2 priceField === 54.95D)
                      withValues (set(nameField === "PISC" & printVersionField === 3 & priceField === 99.99D)) returnErrors ) ~~>
            ( find (Blog) where (titleField === "Programming in Scala") withResults (b => error("Found old Book"))
                    withoutResults success ) ~~>
            ( find (Book) where (nameField === "PISC" and2 printVersionField === 3) withResults {books =>
              books.size should equal (1)
              val b = books(0)
              b.name.value should equal ("PISC")
              b.authors.value should equal (Seq("Martin Odersky", "Lex Spoon", "Bill Venners"))
              b.publisher.value should  equal ("artima")
              b.printVersion.value should equal (3)
              b.price.value should equal (99.99D)
              success
            } withoutResults error("Could not find Book") )
    ) ~~>() verifySuccess
  }
}