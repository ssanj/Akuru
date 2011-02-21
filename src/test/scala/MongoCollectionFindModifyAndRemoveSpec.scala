/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

final class MongoCollectionFindModifyAndRemoveSpec extends CommonSpec {

  import Blog._
  import MongoTypes.MongoObject._
  import MongoTypes.SortOrderEnum._
  "A MongoCollection with findModifyAndRemove" should "not find or remove a non-existant object" in {
    var handlerCalled = false
    (onTestDB ~~>
            drop[Blog] ~~>
            findAndModifyAndRemove(titleField("Storms")) (noSort) {b:Blog => Some("Found an non-existant blog " + b)} {
              handlerCalled = true
              success
            }
    ~~>()) verifySuccess

    handlerCalled should equal (true)
  }

  it should "find and remove an existing object" in {
    var handlerCalled = false
    (onTestDB ~~>
            drop[Blog] ~~>
            save(Blog(title = titleField("Storms"), labels = labelsField(Seq("qld", "weather")))) ~~>
            findAndModifyAndRemove(titleField("Storms")) (noSort) {b:Blog =>
              b.title.value should equal ("Storms")
              b.labels.value should equal (Seq("qld", "weather"))
              success
            } ( throw new RuntimeException("Handler called on success.")) ~~>
            findOne(titleField("Storms")) {b:Blog => Some("Returned deleted Blog") } { handlerCalled = true }
      ~~>()) verifySuccess

    handlerCalled should equal (true)
  }
}