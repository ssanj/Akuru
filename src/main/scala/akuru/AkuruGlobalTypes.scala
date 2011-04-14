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
  type FieldType[O <: DomainObject, T] = akuru.MongoTypes.FieldType[O, T]

  type Field[O <: DomainObject, T] = akuru.MongoTypes.Field[O, T]
  type EmbeddedField[O <: DomainObject, T <: NestedObject] = akuru.MongoTypes.EmbeddedField[O, T]
  type ArrayField[O <: DomainObject, T] = akuru.MongoTypes.ArrayField[O, T]
  type EmbeddedArrayField[O <: DomainObject, T <: NestedObject] = akuru.MongoTypes.EmbeddedArrayField[O, T]

  type NestedField[O <: DomainObject, T] = akuru.MongoTypes.NestedField[O, T]
  type NestedEmbeddedField[O <: DomainObject, T <: NestedObject] = akuru.MongoTypes.NestedEmbeddedField[O, T]
  type NestedArrayField[O <: DomainObject, T] = akuru.MongoTypes.NestedArrayField[O, T]
  type NestedEmbeddedArrayField[O <: DomainObject, T <: NestedObject] = akuru.MongoTypes.NestedEmbeddedArrayField[O, T]
  type NestedFieldValue[O <: DomainObject, T] = NestedField[O, T]#Value
  type Primitive[T] = akuru.MongoTypes.Primitive[T]

  type MongoObject = akuru.MongoTypes.MongoObject
  val MongoObject = akuru.MongoTypes.MongoObject

  type MongoObjectId = akuru.MongoTypes.MongoObjectId
  val MongoObjectId = akuru.MongoTypes.MongoObjectId


  type AkuruObject = akuru.MongoTypes.AkuruObject
  type DomainObject = akuru.MongoTypes.DomainObject
  type NestedObject = akuru.MongoTypes.NestedObject
  val NestedObject = akuru.MongoTypes.NestedObject

  type DomainTemplate[T <: DomainObject] = akuru.MongoTypes.DomainTemplate[T]
  type Template[T <: DomainObject] = akuru.MongoTypes.Template[T]
  type NestedTemplate[T <: DomainObject, N <: NestedObject] = akuru.MongoTypes.NestedTemplate[T, N]

  type MID = akuru.MongoTypes.MID

  type MongoToDomain[T <: DomainObject] = akuru.MongoTypes.MongoToDomain[T]

  type DomainToMongo[T <: DomainObject] = akuru.MongoTypes.DomainToMongo[T]

  type MongoToNested[T <: NestedObject] = akuru.MongoTypes.MongoToNested[T]

  type CollectionName[T <: DomainObject] = akuru.MongoTypes.CollectionName[T]

  type UserFunction = akuru.MongoTypes.UserFunction

  type ToMongo[T] = akuru.domain.serial.ToMongo[T]
  type FromMongo[T] = akuru.domain.serial.FromMongo[T]

  /**
   * A basic type that works on a DomainObject T and returns a result R. An instance of this type represents an unexcecuted Unit of Work.
   */
  type WorkUnit[T <: DomainObject, R] = ConnectionProvider => WorkResult[R]

  /**
   * The result of executing a Unit of Work.
   */
  type WorkResult[R] = Either[String, R]

  /**
   * A function that when supplied with a DatabaseName and a CollectionName return a MongoCollection.
   */
  type ConnectionProvider = (String, String) => akuru.MongoTypes.MongoCollection

  type Executor[T <: DomainObject, R] = akuru.MongoTypes.Executor[T, R]

  type ExecutionResult[R] = akuru.MongoTypes.ExecutionResult[R]
  val ExecutionResult = akuru.MongoTypes.ExecutionResult

  type DBName[T <: DomainObject] = akuru.MongoTypes.DBName[T]

  type DomainTemplateToDBName[T <: DomainObject] = DomainTemplate[T] => DBName[T]

  type DomainObjectToDBName[T <: DomainObject] = T => DBName[T]

  type MongoServer = akuru.MongoTypes.MongoServer

  type AkuruConfig = akuru.MongoTypes.AkuruConfig

  type MongoWriteResult = akuru.MongoTypes.MongoWriteResult
 }