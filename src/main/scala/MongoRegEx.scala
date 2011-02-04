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
  }

  //def regex(t:Tuple2[String, RegEx]): MongoObject =  t._2.toMongo(t._1)
  def regex(tuples:Tuple2[String, RegEx]*): MongoObject =  {
    val mo = empty
    tuples foreach (t => mo.merge(t._2.toMongo(t._1)))
    mo
  }

  case class RegEx(reg:String, flag:RegexConstants.Value) {

    def toMongo(key:String): MongoObject = {
      val mo = empty
      val q = empty
      mo.put("$regex", reg)
      getRegexFlags(flag.id).foreach(mo.put("$options", _))
      q.put(key, mo.toDBObject)
      q
    }

    private def getRegexFlags(f:Int): Option[String] = {
      import org.bson.BSON
      runSafelyWithOptionReturnResult(BSON.regexFlags(f))
    }
  }

  case class RegExWithOptions(reg:String) {
    def / (flag:RegexConstants.Value): RegEx = RegEx(reg, flag)
  }

  implicit def stringToRegX(reg:String): RegExWithOptions = RegExWithOptions(reg)

   implicit def rexToMongoObject(tuple:Tuple2[String, RegEx]): MongoObject = {
     val mo = empty
     mo.merge(tuple._2.toMongo(tuple._1))
     mo
   }

//  implicit def rexToMongoObject(tuples:Tuple2[String, RegEx]*): MongoObject = {
//    val mo = empty
//    tuples foreach (t => mo.merge(t._2.toMongo(t._1)))
//    mo
//  }
}