/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru


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

}