/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

final class MongoObjectSpec extends CommonSpec {

  import Blog._
  import MongoTypes.MongoObject

  "A MongoObject" should "should merge documents with no dupes" in {
    val mo1:MongoObject = new MongoObject().putPrimitive(titleField("blah"))
    val mo2:MongoObject = new MongoObject().putPrimitiveArray[String](labelsField(Seq("test")))
    val mo3 = mo1.mergeDupes(mo2)

    mo1.getPrimitive(titleField) should equal ("blah")
    mo3.getPrimitiveArray(labelsField) should equal (Seq("test"))
  }
}