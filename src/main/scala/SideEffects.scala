/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

trait SideEffects { this:Tools =>

  val newLine = getStringOrDefault(System.getProperty("line.separator"))("\n")

  implicit def addWithNewLine: (String, String) => String = stringAdd(newLine)

}