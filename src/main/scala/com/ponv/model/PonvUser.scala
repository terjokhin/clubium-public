package com.ponv.model

import cats.Show

final case class PonvUser(id: Long, name: String, lastname: Option[String], active: Boolean, lastseen: Int, username: Option[String])

object PonvUser:
  implicit val show: Show[PonvUser] = u =>
    val firstName = u.name
    val lastName  = u.lastname.getOrElse("")
    val username  = u.username.map(un => s"(@$un)").getOrElse("")
    Array(firstName, lastName, username).filterNot(_.isEmpty).mkString(" ")
