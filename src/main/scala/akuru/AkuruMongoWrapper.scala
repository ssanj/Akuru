/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoCursor

trait AkuruMongoWrapper { this:AkuruFunctions =>

  def afind[T <: DomainObject : CollectionName : DBName : MongoToDomain, R](f: => MongoObject)(c: MongoCursor => MongoCursor)(g: Seq[T] => WorkResult[R])
                                                              (h: => WorkResult[R]): WorkUnit[T, R] = {
    cp => {
      cp(implicitly[DBName[T]].name, implicitly[CollectionName[T]].name).find[T](f)(c).fold(l => Left(l), r => if (r.isEmpty) h else g(r))
    }
  }

}