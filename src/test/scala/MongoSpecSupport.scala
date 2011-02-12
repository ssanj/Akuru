/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
import org.scalatest.Assertions
import org.scalatest.matchers.ShouldMatchers

trait MongoSpecSupport { this:Assertions with ShouldMatchers with MongoFunctions =>

  case class VerifyResult(op:Option[String]) {

    def verifySuccess() {
      op match {
        case Some(error) => fail(error)
        case None =>
      }
    }

    def verifyError(f:String => Unit) {
      op match {
        case Some(error) => f(error)
        case None => fail("Expected an error")
      }
    }
  }

  def success: Option[String] = None

  def has(str:String): String => Unit =  result => result should include regex (str)

  implicit def opToVerifyResult(op:Option[String]): VerifyResult = VerifyResult(op)

  def onTestDB = onDatabase("akuru_test")
}