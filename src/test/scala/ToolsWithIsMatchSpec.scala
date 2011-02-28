/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

final class ToolsWithIsMatchSpec extends FlatSpec with ShouldMatchers with Tools {

  "Tools with isMatch" should "match the expected types for AnyVal" in  {
    isMatch[Int](5.asInstanceOf[AnyRef]) should equal (true)
    isMatch[Long](10L.asInstanceOf[AnyRef]) should equal (true)
    isMatch[Char]('A'.asInstanceOf[AnyRef]) should equal (true)
    isMatch[Double](11.11D.asInstanceOf[AnyRef]) should equal (true)
    isMatch[Float](11.11F.asInstanceOf[AnyRef]) should equal (true)
    isMatch[Byte](25.asInstanceOf[Byte].asInstanceOf[AnyRef]) should equal (true)
    isMatch[Short](2000.asInstanceOf[Short].asInstanceOf[AnyRef]) should equal (true)
    isMatch[Boolean](false.asInstanceOf[AnyRef]) should equal (true)
  }

  it should "not match the unexpected types for AnyVal" in  {
    isMatch[Long](5.asInstanceOf[AnyRef]) should equal (false)
    isMatch[Int](10L.asInstanceOf[AnyRef]) should equal (false)
    isMatch[Boolean]('A'.asInstanceOf[AnyRef]) should equal (false)
    isMatch[Float](11.11D.asInstanceOf[AnyRef]) should equal (false)
    isMatch[Double](11.11F.asInstanceOf[AnyRef]) should equal (false)
    isMatch[Short](25.asInstanceOf[Byte].asInstanceOf[AnyRef]) should equal (false)
    isMatch[Byte](2000.asInstanceOf[Short].asInstanceOf[AnyRef]) should equal (false)
    isMatch[Char](false.asInstanceOf[AnyRef]) should equal (false)
  }

  it should "match other types" in {
    case class Person(name:String)
    case class Employee(id:Int)

    isMatch[String]("blah".asInstanceOf[AnyRef]) should equal (true)
    isMatch[Person]("blah".asInstanceOf[AnyRef]) should equal (false)

    isMatch[Person](Person("blah").asInstanceOf[AnyRef]) should equal (true)
    isMatch[Employee](Person("blah").asInstanceOf[AnyRef]) should equal (false)

    isMatch[Employee](Employee(5000).asInstanceOf[AnyRef]) should equal (true)
    isMatch[Person](Employee(5000).asInstanceOf[AnyRef]) should equal (false)
  }

  it should "match assignable types" in {
    class A
    class B extends A

    isMatch[A](new B().asInstanceOf[AnyRef]) should equal (true)
    isMatch[B](new B().asInstanceOf[AnyRef]) should equal (true)

    isMatch[A](new A().asInstanceOf[AnyRef]) should equal (true)
    isMatch[B](new A().asInstanceOf[AnyRef]) should equal (false)
  }
}
