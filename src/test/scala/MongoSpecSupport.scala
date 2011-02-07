/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
import org.scalatest.Assertions

trait MongoSpecSupport { this:Assertions with MongoFunctions =>

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

  implicit def opToVerifyResult(op:Option[String]): VerifyResult = VerifyResult(op)

  def onTestDB = onDatabase("akuru_test")
}