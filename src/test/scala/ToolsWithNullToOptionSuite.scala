/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

final class ToolsWithNullToOptionSuite extends FunSuite with ShouldMatchers with Tools {

  test("NullToOption should convert null to None") {
    nullToOption(null) should equal (None)
  }

  test("NullToOption should convert a function that returns null to None") {
    case class Box[T](val contents:T)
    nullToOption(Box(null).contents) should equal (None)
  }

  test("NullToOption should convert a non-null to Some") {
    nullToOption("testing".toUpperCase) should equal (Some("TESTING"))
  }
}
