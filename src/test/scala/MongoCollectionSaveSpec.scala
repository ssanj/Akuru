/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */


package akuru

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

final class MongoCollectionSaveSpec extends FlatSpec with ShouldMatchers with DomainObjects with MongoFunctions with DomainSupport {

  def success: Option[String] = None

  "A MongoCollection" should "save a new MongoObject" in {
    val con = onDatabase("akuru_test")
    val result = { con ~~>
                      (drop[Blog] _) ~~>
                      ( findOne("title" -> "blah") { t:Blog => fail("Shouldn't have found Blog")} { ignoreError } _) ~~>
                      ( save(Blog(title = "blah", labels = Seq("test", "random"))) _) ~~>
                      ( findOne("title" -> "blah") { t:Blog =>
                          t.title should equal ("blah")
                          t.labels should equal (Seq("test", "random"))
                          success
                      } { () => fail("Didn't find Blog") } _)
                 } ~~>()

    verifySuccess(result)
  }

  def verifySuccess(op:Option[String]) {
    op match {
      case Some(error) => fail(error)
      case None =>
    }
  }

}