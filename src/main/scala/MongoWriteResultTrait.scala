/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes._
import com.mongodb.{WriteResult}

trait MongoWriteResultTrait extends WrapWithTrait with Tools with SideEffects {

  trait WriteResultTrait {
    def getError: Option[String]
    def getLastErrorTrace: Option[String]
  }

  case class MongoWriteResult(wr:WriteResultTrait) {
    def getMongoError: Option[MongoError] = {
      wrapWith[Option[MongoError]] {
        wr.getError.flatMap(e => wr.getLastErrorTrace.map(t => MongoError(e, t)))
      }.fold(Some(_), identity)
    }

    def getStringError: Option[String] = mongoErrorToString(getMongoError)

    def mongoErrorToString(me:Option[MongoError]): Option[String] = me.map(e => addWithNewLine(e.message, e.stackTrace))
  }

  object MongoWriteResult extends Tools  {
    implicit def writeResultToMongoWriteResult(wr:WriteResult): MongoWriteResult =
      MongoWriteResult(new WriteResultTrait {
        def getError = nullToOption(wr.getError)
        def getLastErrorTrace = {
          nullToOption(wr.getLastError).flatMap(x => nullToOption(x.getException)).flatMap(y => nullToOption(y.getStackTraceString))
        }
      })
  }
}
