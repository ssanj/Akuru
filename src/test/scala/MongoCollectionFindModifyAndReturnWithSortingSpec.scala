/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

final class MongoCollectionFindModifyAndReturnWithSortingSpec extends CommonSpec {

  import Book._
  import MongoTypes.MongoObject._

  "A MongoCollection with findAndModifyAndReturnWithSorting" should "find an sorted object sorted ascending" in {
    verifySort(ASC, createVersion1, 2)
  }

  "A MongoCollection with findAndModifyAndReturnWithSorting" should "find an sorted object sorted descending" in {
    verifySort(DSC, createVersion2, 1)
  }
  private def setup:FutureConnection = {
    onTestDB ~~>
    drop[Book] ~~>
    save(Book(name = nameField("Programming in Scala"),
              authors = authorsField(Seq("Martin Odersky", "Lex Spoon", "Bill Venners")),
              publisher = publisherField("artima"),
              printVersion = printVersionField(2), /* v2 */
              price = priceField(54.95D))) ~~>
    save(Book(name = nameField("Programming in Scala"),
              authors = authorsField(Seq("Martin Odersky", "Lex Spoon", "Bill Venners")),
              publisher = publisherField("artima"),
              printVersion = printVersionField(1), /* v1 */
              price = priceField(40.00D)))
  }

  private def verifySort(sortOrder:SortOrder, updated:Book, unUpdatedVersion:Int) {
   (setup ~~>
              findAndModifyAndReturn(nameField("Programming in Scala"))(sort(printVersionField, sortOrder)) { updated }{ b:Book =>
                b.name.value should equal (updated.name.value)
                success
              } { Some("Book was not updated!!") } ~~>
              find(nameField("Programming in Scala")) { books:Seq[Book] =>
                books.size should equal (1)
                books.foreach { b:Book =>
                  b.name.value should equal ("Programming in Scala")
                  b.printVersion.value should equal (unUpdatedVersion)
                }
                success
    } ~~>()) verifySuccess
  }

  private def createVersion1: Book = {
    Book(name = nameField("Programming in Scala v1"),
          authors = authorsField(Seq("Martin Odersky", "Lex Spoon", "Bill Venners")),
          publisher = publisherField("artima"),
          printVersion = printVersionField(1),
          price = priceField(40.00D))

  }

  private def createVersion2: Book = {
    Book(name = nameField("Programming in Scala v2"),
          authors = authorsField(Seq("Martin Odersky", "Lex Spoon", "Bill Venners")),
          publisher = publisherField("artima"),
          printVersion = printVersionField(2),
          price = priceField(40.00D))
  }
}