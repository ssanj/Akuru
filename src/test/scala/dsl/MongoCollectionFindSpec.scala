/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package dsl

final class MongoCollectionFindSpec extends CommonSpec with FindDSL with DSLTools {

  import Blog._

  "A MongoCollection with find" should "find all saved objects matching a query" in {
    (onTestDB ~~>
              drop[Blog] ~~>
              save(Blog(titleField === "sample1", labelsField === Seq("sample"))) ~~>
              save(Blog(titleField === "sample2", labelsField === Seq("sample"))) ~~>
              save(Blog(titleField === "sample3", labelsField === Seq("sample"))) ~~>
              ( find * Blog where titleField === ("sample*"/) withResults { blogs =>
                blogs.size should equal (3)
                blogs.exists(_.title.value == "sample1") should be (true)
                blogs.exists(_.title.value == "sample2") should be (true)
                blogs.exists(_.title.value == "sample3") should be (true)
                success
              } withoutResults error("Expected 3 but got 0"))
    ) ~~>() verifySuccess
  }

  it should "return zero results if there are no matches" in {
    (onTestDB ~~>
              drop[Blog] ~~>
              ( find * Blog where titleField === ("*"/) withResults { b => error("Expected 0 but received: " + b.size) } withoutResults success)
    ) ~~>() verifySuccess
  }

  it should "handle exceptions thrown on finder execution" in {
    (
      onTestDB ~~>
              drop[Blog] ~~>
              (find * Person where Person.nameField === ("*"/) withResults (_ => error("Should not have return results"))
              withoutResults error("should not have been called when an Exception is thrown!")) ~~>()
    ) verifyError has (Person.expectedError)
  }

  it should "handle exceptions throw on creating a query" in {
    (
      onTestDB ~~>
              drop[Blog] ~~>
              ( find * Blog where (exceptionalFieldValueJoiner) withResults (_ => error("Should not have return results"))
              withoutResults error("should not have been called when an Exception is thrown!") ) ~~>()
    ) verifyError has (mongoCreationException)
  }

  it should "handle exception thrown by match handler function" in {
    (
      onTestDB ~~>
              drop[Blog] ~~>
              save(Blog(titleField === "Querying with RegEx", labelsField === Seq("query", "regex"))) ~~>
              ( find * Blog where titleField === (".*"/) withResults (_ => ex("Handler threw an Exception"))
              withoutResults error("should not have been called when an Exception is thrown!") ) ~~>()
    ) verifyError has ("Handler threw an Exception")
  }

  it should "find regex" in {
    (
      onTestDB ~~>
              drop[Blog] ~~>
              save(Blog(titleField === "Querying with RegEx", labelsField === Seq("query", "regex"))) ~~>
              ( find * Blog where titleField === ("querying with RegEx"/) withResults { b => error("Expected 0 but received: " + b.size) }
                      withoutResults success ) ~~>
              ( find * Blog where titleField === ("Querying with RegEx"/i) withResults { expectOne } withoutResults error("Expected 1") ) ~~>
              ( find * Blog where titleField === (".* with RegEx"/) withResults { expectOne } withoutResults error("Expected 1") ) ~~>
              ( find * Blog where titleField === (".*query.*"/i) withResults { expectOne } withoutResults error("Expected 1"))
      ~~>()) verifySuccess
  }

  private def expectOne(blogs:Seq[Blog]): Option[String] = { blogs.size should equal (1); success }

  it should "sort results" in {
    (onTestDB ~~>
            drop[Blog] ~~>
            save(Blog(titleField === "Pears", labelsField === Seq("fruit", "pears"))) ~~>
            save(Blog(titleField === "Orange", labelsField === Seq("citrus", "fruit", "navel", "jaffa"))) ~~>
            save(Blog(titleField === "Apple", labelsField === Seq("apples", "fruit", "green", "red"))) ~~>
            save(Blog(titleField === "WaterMellon", labelsField === Seq("mellon", "fruit", "striped"))) ~~>
            ( find * Blog where labelsField === ("fruit"/) constrainedBy (Limit(2) and Order(titleField -> ASC)) withResults {b =>
              b.size should equal (2)
              b(0).title.value should equal ("Apple")
              b(1).title.value should equal ("Orange")
              success
            } withoutResults error("Expected 2 but received 0."))
    ) ~~>()  verifySuccess
  }
}

