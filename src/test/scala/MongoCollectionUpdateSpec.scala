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
  "A MongoCollection with Updates" should "update an existing value" in {
    ( onTestDB ~~>
            drop[Blog] ~~>
            save(Blog(title = titleField("Blog updates"), labels = labelsField(Seq("blog, update")))) ~~>
            findOne(Blog.titleField.name -> "Blog updates") { b:Blog => ignoreSuccess } {  throw new RuntimeException("Could not find Blog") } ~~>
            update(Blog.titleField.name -> "Blog updates" ) { set(Blog.titleField("Blog Updatees")) } ~~>
            findOne(Blog.titleField.name -> "Blog updates") { b:Blog => throw new RuntimeException("found old Blog") } {  ignoreError }  ~~>
            findOne(Blog.titleField.name -> "Blog Updatees") { b:Blog =>
              b.title.value should equal ("Blog Updatees")
              success
            } {  throw new RuntimeException("Could not find updated Blog")
    } ~~>()) verifySuccess
  }
}