/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import org.scalatest.{FlatSpec, Assertions}
import org.scalatest.matchers.ShouldMatchers
import akuru.dsl.{AkuruDSL2, DSLTools}
import akuru.domain.TestDomainObjects

trait AkuruSpecSupport extends FlatSpec
                            with ShouldMatchers
                            with AkuruMongoWrapper
                            with AkuruDSL2
                            with TestDomainObjects
                            with DSLTools { this:Assertions =>

  object Config extends AkuruConfig { val defaultDBName = "akuru_test" }

  implicit def workResultToVerifyWorkResult[R](wr:WorkResult[R]): VerifyWorkResult[R] = VerifyWorkResult[R](wr)

  case class VerifyWorkResult[R](r:WorkResult[R]) {

    def verifySuccess(value:R) {
      r match {
        case Left(e) => fail("Expected Success but got Failure(" + e + ")")
        case Right(s) => s should equal (value)
      }
    }

    def verifySuccess {
      r match {
        case Left(e) => fail("Expected Success but got Failure(" + e + ")")
        case Right(_) =>
      }
    }

    def verifyFailure(error:String) {
      r match {
        case Left(e) => e should equal (error)
        case Right(s) => fail("Expected Failure bot got Success(" + s + ")")
      }
    }
  }

  def ex(error:String): WorkResult[Nothing] = throw new RuntimeException(error)
}