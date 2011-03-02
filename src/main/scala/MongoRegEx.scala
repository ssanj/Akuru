/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._
import MongoTypes.MongoObject.empty
import MongoTypes.MongoObject.mongo

trait MongoRegEx {

  object RegexConstants extends Enumeration {

    import java.util.regex.Pattern

    val canonical = Value(Pattern.CANON_EQ)
    val i = Value(Pattern.CASE_INSENSITIVE)
    val x = Value(Pattern.COMMENTS)
    val dot = Value(Pattern.DOTALL)
    val literal = Value(Pattern.LITERAL)
    val m = Value(Pattern.MULTILINE)
    val u = Value(Pattern.UNICODE_CASE)
    val d = Value(Pattern.UNIX_LINES)
    val none = Value(-1) //default
  }
  case class RegEx(reg: String, flag: Option[RegexConstants.Value] = None) {

    def toMongo(key: String): MongoObject = {
      val mo = mongo.putPrimitiveObject("$regex", reg)
      mongo.putMongo(key, foldOption(flag)(mo)(f => foldOption(getRegexFlags(f.id))(mo)(mo.putPrimitiveObject("$options", _))))
    }

    private def getRegexFlags(f: Int): Option[String] = {
      import org.bson.BSON
      runSafelyWithOptionReturnResult(BSON.regexFlags(f))
    }
  }

  case class RegExWithOptions(reg: String) {
    def /(implicit flag: RegexConstants.Value): RegEx = {
      flag match {
        case RegexConstants.none => RegEx(reg)
        case _ => RegEx(reg, Some(flag))
      }
    }
  }

  case class FieldRegEx[T](field:Field[T]) {
    def ?*(reg:RegEx): MongoObject = reg.toMongo(field.name)
  }
}