/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import _root_.akuru.MongoTypes._
import _root_.akuru.MongoTypes.MongoObject._

final class MongoCollectionUpdateSpec extends FlatSpec with ShouldMatchers
        with TestDomainObjects
        with MongoFunctions
        with MongoSpecSupport
        with Tools {

  import Blog._
  import Book._
  "A MongoCollection with Updates" should "update an existing value" in {
    ( onTestDB ~~>
            drop[Blog] ~~>
            save(Blog(title = titleField("Blog updates"), labels = labelsField(Seq("blog, update")))) ~~>
            findOne(titleField("Blog updates")) { b:Blog => ignoreSuccess } {  throw new RuntimeException("Could not find Blog") } ~~>
            update[Blog](titleField("Blog updates")) { set(titleField("Blog Updatees")) } ~~>
            findOne(titleField("Blog updates")) { b:Blog => throw new RuntimeException("found old Blog") } {  ignoreError }  ~~>
            findOne(titleField("Blog Updatees")) { b:Blog =>
              b.title.value should equal ("Blog Updatees")
              success
            } {  throw new RuntimeException("Could not find updated Blog") } ~~>
            drop[Book] ~~>
            save(Book(name = nameField("Programming in Scala"),
                      authors = authorsField(Seq("Martin Odersky", "Lex Spoon", "Bill Venners")),
                      publisher = publisherField("artima"),
                      printVersion = printVersionField(2),
                      price = priceField(54.95D))
            ) ~~>
            findOne(nameField("Programming in Scala")) { b:Book =>
              b.name.value should equal ("Programming in Scala")
              b.authors.value should equal (Seq("Martin Odersky", "Lex Spoon", "Bill Venners"))
              b.publisher.value should  equal ("artima")
              b.printVersion.value should equal (2)
              b.price.value  should equal (54.95D)
              success
            } { throw new RuntimeException("Could not find Book") } ~~>
            update[Book](publisherField("artima")) { set(nameField("PISC"), printVersionField(3), priceField(99.99D))} ~~>
            findOne(nameField("Programming in Scala")) {b:Book => throw new RuntimeException("Found old Book") } { ignoreError } ~~>
            findOne(nameField("PISC")) {b:Book =>
              b.name.value should equal ("PISC")
              b.authors.value should equal (Seq("Martin Odersky", "Lex Spoon", "Bill Venners"))
              b.publisher.value should  equal ("artima")
              b.printVersion.value should equal (3)
              b.price.value  should equal (99.99D)
              success
            } { throw new RuntimeException("Could not find Book")
    } ~~>()) verifySuccess
  }
}