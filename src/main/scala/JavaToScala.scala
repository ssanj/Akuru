/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

trait JavaToScala {

  def nullToOption[A](f: => A): Option[A] = {
    val result = f
    if (result == null) None else Some(result)
  }
}
