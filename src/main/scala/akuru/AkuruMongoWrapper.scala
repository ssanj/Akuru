/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoCursor
import Tools._

trait AkuruMongoWrapper {

  def afind[T <: DomainObject : CollectionName : DBName : MongoToDomain, R](f: => MongoObject)(c: MongoCursor => MongoCursor)
     (success: Seq[T] => WorkResult[R])(failure: => WorkResult[R]): WorkUnit[T, R] =  cp => {
      cp(implicitly[DBName[T]].name, implicitly[CollectionName[T]].name).aFind[T](f)(c).fold(l => Left(l), r =>
        if (r.isEmpty) failure else success(r))
  }

  def aSave[T <: DomainObject : DomainToMongo : CollectionName : DBName, R](f: => T)(success:  => WorkResult[R])
    (failure: MongoWriteResult => WorkResult[R]): WorkUnit[T, R] = cp => {
    cp(implicitly[DBName[T]].name, implicitly[CollectionName[T]].name).aSave[T](f).fold(l => Left(l), wr => if (wr.ok) success else failure(wr))
  }

  def aSaveMany[T <: DomainObject : DomainToMongo : CollectionName : DBName, R](doms: => Seq[T])(success:  => WorkResult[R])
    (failure: (T, MongoWriteResult) => WorkResult[R]): WorkUnit[T, R] = cp => {
    val col = cp(implicitly[DBName[T]].name, implicitly[CollectionName[T]].name)

    case class FailedSave[T](dom:T, wr:MongoWriteResult)

    type MultipleSaveResult = Either[String, Option[FailedSave[T]]]

    val results:MultipleSaveResult =
      doms.foldLeft(Right(None):MultipleSaveResult){
        case (acc, dom) => acc match {
          case l @ Left(_) => l
          case Right(None) => col.aSave[T](dom) match {
            case Left(e) => Left(e)
            case Right(wr) => if (wr.ok) Right(None) else Right(Some(FailedSave[T](dom, wr)))
          }
          case r @ Right(Some(_)) => r
        }
      }

    results.fold(l => Left(l), r => r match {
      case None => success
      case Some(failedSave) => failure(failedSave.dom, failedSave.wr)
    })
  }

  def aDrop[T <: DomainObject : CollectionName : DBName, R](success: => WorkResult[R]): WorkUnit[T, R] = cp =>
    cp(implicitly[DBName[T]].name, implicitly[CollectionName[T]].name).aDrop.fold(l => Left(l), r => success)
}