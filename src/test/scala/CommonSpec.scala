/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

trait CommonSpec extends FlatSpec with ShouldMatchers
        with TestDomainObjects
        with MongoFunctions
        with MongoSpecSupport
        with Tools