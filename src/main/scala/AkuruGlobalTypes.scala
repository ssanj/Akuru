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

  type MongoObject = akuru.MongoTypes.MongoObject
  val MongoObject = akuru.MongoTypes.MongoObject

  type MongoObjectId = akuru.MongoTypes.MongoObjectId
  val MongoObjectId = akuru.MongoTypes.MongoObjectId

  type DomainObject = akuru.MongoTypes.DomainObject
  val DomainObject = akuru.MongoTypes.DomainObject
  type DomainTemplate[T <: DomainObject] = akuru.MongoTypes.DomainTemplate[T]

  type MID = akuru.MongoTypes.MID

  type MongoToDomain[T <: DomainObject] = akuru.MongoTypes.MongoToDomain[T]

  type DomainToMongo[T <: DomainObject] = akuru.MongoTypes.DomainToMongo[T]

  type CollectionName[T <: DomainObject] = akuru.MongoTypes.CollectionName[T]

  type UserFunction = akuru.MongoTypes.UserFunction
 }