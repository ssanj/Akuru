/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec


final class ToolsWithNullSafeExceptionMessageSpec extends FlatSpec with ShouldMatchers with Tools {

  "Tools with nullSafeExceptionMessage" should "return a valid Exception message" in {
    nullSafeExceptionMessage(new RuntimeException("Valid Message")) should equal ("Valid Message")
  }

  it should "return a default message with StackTrace if the Exception message is null" in {
    var unstable:String = null
    try {
      unstable.toString
      fail("expected a NullPointerException")
    } catch {
      case ex:Exception => {
        val errors = nullSafeExceptionMessage(ex).split("\n")
        errors.length should be > (1)
        errors(0) should equal (defaultExceptionMessage)
      }
    }
  }
}