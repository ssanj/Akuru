/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

trait MongoTools { this:SideEffects =>

  import MongoTypes.MongoError
  def mongoErrorToOptionString(me:Option[MongoError]): Option[String] = me.map(e => addWithNewLine(e.message, e.stackTrace))
}