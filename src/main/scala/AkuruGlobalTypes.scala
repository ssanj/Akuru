/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

trait AkuruGlobalTypes {
  type SortOrder = akuru.MongoTypes.SortOrder.Value

  val ASC = akuru.MongoTypes.SortOrder.ASC
  val DSC = akuru.MongoTypes.SortOrder.DSC

  val canonical = akuru.MongoTypes.RegexConstants.canonical
  val i = akuru.MongoTypes.RegexConstants.i
  val x = akuru.MongoTypes.RegexConstants.x
  val dot = akuru.MongoTypes.RegexConstants.dot
  val literal = akuru.MongoTypes.RegexConstants.literal
  val m = akuru.MongoTypes.RegexConstants.m
  val u = akuru.MongoTypes.RegexConstants.u
  val d = akuru.MongoTypes.RegexConstants.d
  val none = akuru.MongoTypes.RegexConstants.none

  type FieldValue[T] = akuru.MongoTypes.FieldValue[T]
  type Field[T] = akuru.MongoTypes.Field[T]

}