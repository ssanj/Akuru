/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import _root_.akuru.MongoTypes._
import _root_.akuru.MongoTypes.MongoObject._

final class MongoCollectionUpdateSpec extends CommonSpec {

  import Blog._
  import Book._
  "A MongoCollection with Updates" should "update an existing value" in {
    ( onTestDB ~~>
            drop[Blog] ~~>
            safeUpdate[Blog](titleField("Blog updates"))(set(titleField("Phantom updates"))) {wr: MongoWriteResult =>
              runSafelyWithOptionReturnError {
                wr.updatedExisting should equal (false)
                wr.getN should equal (Some(0))
                wr.ok should equal (true)
            }} ~~>
            save(Blog(title = titleField("Blog updates"), labels = labelsField(Seq("blog, update")))) ~~>
            findOne(titleField("Blog updates")) { b:Blog => ignoreSuccess } {  throw new RuntimeException("Could not find Blog") } ~~>
            update[Blog](titleField("Blog updates")) { set(titleField("Blog Updatees"), labelsField(Seq("bl%%g")).splat) } ~~>
            findOne(titleField("Blog updates")) { b:Blog => throw new RuntimeException("found old Blog") } {  noOp }  ~~>
            findOne(titleField("Blog Updatees")) { b:Blog =>
              b.title.value should equal ("Blog Updatees")
              b.labels.value should equal (Seq("bl%%g"))
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
            update[Book](combine(publisherField("artima"), printVersionField(2), priceField(54.95D)))
                      { set(nameField("PISC"), printVersionField(3),priceField(99.99D))} ~~>
            findOne(nameField("Programming in Scala")) {b:Book => throw new RuntimeException("Found old Book") } { noOp } ~~>
            findOne(nameField("PISC") and printVersionField(3)) {b:Book =>
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