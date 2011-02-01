/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

trait MongoErrorTrait {

    /**
   * Captures an <code>Exception</code>'s error message and stacktrace and allows for the unification
   * of errors returned by <code>MongoType</code>s.
   */
  case class MongoError(val message:String, val stackTrace:String) {
    def this(ex:Exception) = this(ex.getMessage, ex.getStackTraceString)
  }

}
