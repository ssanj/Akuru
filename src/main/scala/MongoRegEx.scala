/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._
import MongoTypes.MongoObject.empty

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

   type KeyedRegEx = Tuple2[String, RegEx]

  case class RegEx(reg: String, flag: Option[RegexConstants.Value] = None) {

    def toMongo(key: String): MongoObject = {
      val mo = empty
      val q = empty
      mo.put("$regex", reg)
      flag.foreach(f => getRegexFlags(f.id).foreach(mo.put("$options", _)))
      q.put(key, mo.toDBObject)
      q
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

  def regExToMongo(tuples: KeyedRegEx*): MongoObject = {
    val mo = empty
    tuples foreach (t => mo.merge(t._2.toMongo(t._1)))
    mo
  }

  def regex(tuples: KeyedRegEx*): MongoObject = regExToMongo(tuples:_*)

  implicit def defaultRegExOption: RegexConstants.Value = RegexConstants.none

  implicit def stringToRegX(reg: String): RegExWithOptions = RegExWithOptions(reg)

  implicit def regexTuple1ToMongoObject(tuple: KeyedRegEx): MongoObject = regExToMongo(tuple)

  implicit def regexTuple2ToMongoObject(tuples: Tuple2[KeyedRegEx, KeyedRegEx]): MongoObject = regExToMongo(tuples._1, tuples._2)

  implicit def regexTuple3ToMongoObject(tuples: Tuple3[KeyedRegEx, KeyedRegEx, KeyedRegEx]): MongoObject =
    regExToMongo(tuples._1, tuples._2, tuples._3)

  implicit def regexTuple4ToMongoObject(tuples: Tuple4[KeyedRegEx, KeyedRegEx, KeyedRegEx, KeyedRegEx]): MongoObject =
    regExToMongo(tuples._1, tuples._2, tuples._3, tuples._4)

  implicit def regexTuple5ToMongoObject(tuples: Tuple5[KeyedRegEx, KeyedRegEx, KeyedRegEx, KeyedRegEx, KeyedRegEx]): MongoObject =
    regExToMongo(tuples._1, tuples._2, tuples._3, tuples._4, tuples._5)
}