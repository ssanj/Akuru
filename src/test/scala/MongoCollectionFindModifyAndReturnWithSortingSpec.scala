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
  private def setup: FutureConnection = {
    onTestDB ~~>
    drop[Book] ~~>
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
              findAndModifyAndReturn[Book](nameField("Programming in Scala"))(sort(printVersionField, sortOrder)) { updated }{ b =>
                b.name.value should equal (updated.name.value)
                success
              } { Some("Book was not updated!!") } ~~>
              find[Book](nameField("Programming in Scala")) { all }  { books =>
                books.size should equal (1)
                books.foreach { b:Book =>
                  b.name.value should equal ("Programming in Scala")
                  b.printVersion.value should equal (unUpdatedVersion)
                }
                success}
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