/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

trait AkuruGlobalTypes {
  type SortOrder = akuru.MongoTypes.SortOrder.Value
  val ASC = akuru.MongoTypes.SortOrder.ASC
  val DSC = akuru.MongoTypes.SortOrder.DSC
}