package com.ponv.bot

import telegramium.bots.User

extension (user: User) {

  def getAppeal: String = user.username.map("@" + _).getOrElse(user.firstName)
}
