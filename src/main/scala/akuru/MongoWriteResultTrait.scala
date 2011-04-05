/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import Tools._

trait MongoWriteResultTrait extends WrapWithTrait {

  import MongoTypes.MongoError
  import MongoTypes.mongoErrorToOptionString
  import com.mongodb.WriteResult

  trait WriteResultTrait {
    def getError: Option[String]
    def getLastErrorTrace: Option[String]
    def getN: Option[Int]
    def getFields: Map[String, AnyRef]

  }

  object WriteResultTrait {
    def create(wr:WriteResult): WriteResultTrait = new WriteResultTrait {
        def getError: Option[String] = nullToOption(wr.getError)

        def getLastErrorTrace: Option[String] = {
          nullToOption(wr.getLastError).flatMap(x => nullToOption(x.getException)).flatMap(y => nullToOption(y.getStackTraceString))
        }

        def getN: Option[Int] = nullToOption(wr.getN)

        import scala.collection.JavaConversions._

        def getFields: Map[String, AnyRef] = asScalaMap[String, AnyRef](wr.getLastError()).toMap
      }
  }

  case class MongoWriteResult(wr:WriteResultTrait) {
    def getMongoError: Option[MongoError] = {
      wrapWith[Option[MongoError]] {
        wr.getError.flatMap(e => wr.getLastErrorTrace.map(t => MongoError(e, t)))
      }.fold(Some(_), identity)
    }

    def getStringError: Option[String] = mongoErrorToOptionString(getMongoError)

    def getField(name: String): Option[AnyRef] = runSafelyWithOptionReturnResult[AnyRef](wr.getFields(name))

    def getFieldorElse(name: String, default: => AnyRef): AnyRef =  foldOption(getField(name))(default)(identity)

    def getN: Option[Int] = wr.getN

    def ok: Boolean = booleanFold(getField("ok"))

    def updatedExisting: Boolean = booleanFold(getField("updatedExisting"))

    private def booleanFold(op:Option[AnyRef]): Boolean = foldOption(op)(false)(toBoolean)

    def getFields: Map[String, AnyRef] = wr.getFields
  }

  object MongoWriteResult {
    import WriteResultTrait._
    implicit def writeResultToMongoWriteResult(wr:WriteResult): MongoWriteResult = MongoWriteResult(create(wr))
  }
}
