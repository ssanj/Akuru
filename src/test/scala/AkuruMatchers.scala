/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import org.scalatest.matchers.HavePropertyMatcher

trait AkuruMatchers {
  def name(expectedValue: String) =
      new HavePropertyMatcher[Field[_, _], String] {
        import org.scalatest.matchers.HavePropertyMatchResult
        def apply(field: Field[_, _]) =
          HavePropertyMatchResult(
            field.name == expectedValue,
            "name",
            expectedValue,
            field.name
          )
      }
}