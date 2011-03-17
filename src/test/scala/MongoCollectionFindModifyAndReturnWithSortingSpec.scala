/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

final class MongoCollectionFindModifyAndReturnWithSortingSpec extends CommonSpec with AkuruDSL {

  import Book._
  import MongoTypes.MongoObject._

  "A MongoCollection with findAndModifyAndReturnWithSorting" should "find an sorted object sorted ascending" in {
    verifySort(ASC, createVersion1, 2)
  }

  "A MongoCollection with findAndModifyAndReturnWithSorting" should "find an sorted object sorted descending" in {
    verifySort(DSC, createVersion2, 1)
  }
  private def setup: FutureConnection = {
    initBook ~~>
    save(Book(nameField("Programming in Scala"),
              authorsField(Seq("Martin Odersky", "Lex Spoon", "Bill Venners")),
              publisherField("artima"),
              printVersionField(2), /* v2 */
              priceField(54.95D))) ~~>
    save(Book(nameField("Programming in Scala"),
              authorsField(Seq("Martin Odersky", "Lex Spoon", "Bill Venners")),
              publisherField("artima"),
              printVersionField(1), /* v1 */
              priceField(40.00D)))
  }

  private def verifySort(sortOrder:SortOrder, updated:Book, unUpdatedVersion:Int) {
   (setup ~~>
              ( modify a Book where nameField === "Programming in Scala" using sort(printVersionField, sortOrder) updateWith updated
                  withUpdated{ b =>
                      b.name.value should equal (updated.name.value)
                      success
                  } onError error("Book was not updated!!") ) ~~>
              ( find (Book) where nameField === "Programming in Scala" withResults { books =>
                books.size should equal (1)
                books.foreach { b:Book =>
                  b.name.value should equal ("Programming in Scala")
                  b.printVersion.value should equal (unUpdatedVersion)
                }
                success} withoutResults error("Expected 1 but got 0 hits") )
   ) ~~>() verifySuccess
  }

  private def createVersion1: Book = {
    Book(nameField("Programming in Scala v1"),
          authorsField(Seq("Martin Odersky", "Lex Spoon", "Bill Venners")),
          publisherField("artima"),
          printVersionField(1),
          priceField(40.00D))
  }

  private def createVersion2: Book = {
    Book(nameField("Programming in Scala v2"),
          authorsField(Seq("Martin Odersky", "Lex Spoon", "Bill Venners")),
          publisherField("artima"),
          printVersionField(2),
          priceField(40.00D))
  }
}