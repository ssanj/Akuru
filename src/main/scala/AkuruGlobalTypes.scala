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

  type FieldValue[O <: DomainObject, T] = FieldType[O, T]#Value
  type Field[O <: DomainObject, T] = akuru.MongoTypes.Field[O, T]
  type NestedField[O <: DomainObject, T] = akuru.MongoTypes.NestedField[O, T]
  type NestedFieldValue[O <: DomainObject, T] = NestedField[O, T]#Value
  type FieldType[O <: DomainObject, T] = akuru.MongoTypes.FieldType[O, T]

  type MongoObject = akuru.MongoTypes.MongoObject
  val MongoObject = akuru.MongoTypes.MongoObject

  type MongoObjectId = akuru.MongoTypes.MongoObjectId
  val MongoObjectId = akuru.MongoTypes.MongoObjectId

  type DomainObject = akuru.MongoTypes.DomainObject
  type NestedObject = akuru.MongoTypes.NestedObject
  type DomainTemplate[T <: DomainObject] = akuru.MongoTypes.DomainTemplate[T]
  type Template[T <: DomainObject] = akuru.MongoTypes.Template[T]
  type NestedTemplate[T <: DomainObject, N <: NestedObject] = akuru.MongoTypes.NestedTemplate[T, N]

  type MID = akuru.MongoTypes.MID

  type MongoToDomain[T <: DomainObject] = akuru.MongoTypes.MongoToDomain[T]

  type DomainToMongo[T <: DomainObject] = akuru.MongoTypes.DomainToMongo[T]

  type NestedToMongo[T <: NestedObject] = akuru.MongoTypes.NestedToMongo[T]

  type MongoToNested[T <: NestedObject] = akuru.MongoTypes.MongoToNested[T]

  type CollectionName[T <: DomainObject] = akuru.MongoTypes.CollectionName[T]

  type UserFunction = akuru.MongoTypes.UserFunction
 }