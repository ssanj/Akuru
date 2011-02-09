/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoObject

trait MongoConverter[T] {
  def convert(mo:MongoObject): T
  def convert(domain:T): MongoObject
}

trait AnyRefConverter[T] {
  def convert(anyRef:AnyRef): T
}

object AnyRefConverter {

  implicit object LongConverter extends AnyRefConverter[Long] {
    def convert(anyRef:AnyRef): Long = anyRef.toString.toLong
  }

  implicit object IntConverter extends AnyRefConverter[Int] {
    def convert(anyRef:AnyRef): Int = anyRef.toString.toInt
  }

  implicit object DoubleConverter extends AnyRefConverter[Double] {
    def convert(anyRef:AnyRef): Double = anyRef.toString.toDouble
  }

  implicit object StringConverter extends AnyRefConverter[String] {
    def convert(anyRef:AnyRef): String = anyRef.toString
  }

  implicit object BooleanConverter extends AnyRefConverter[Boolean] {
    def convert(anyRef:AnyRef): Boolean = anyRef.toString.toLowerCase match {
      case "true" => true
      case _ => false
    }
  }
}
