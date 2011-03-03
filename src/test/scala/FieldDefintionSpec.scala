/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import org.scalatest.FlatSpec
import MongoTypes.{Field => FD}
import org.scalatest.matchers.ShouldMatchers

final class FieldDefintionSpec extends FlatSpec with ShouldMatchers with AkuruMatchers {

  "A Field" should "match another Field with the same name" in {
    FD[String]("title") should have (name("title"))
  }

  it should "not match another Field with a different name" in {
    FD[String]("title") should not have (name("blah"))
  }
}
