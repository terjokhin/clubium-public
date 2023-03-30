package com.ponv.utils

def getPercentSuffix(num: Int): String =
  val suffix = num % 10 match
    case 1 if num != 11                          => ""
    case rem if isApplicableForSuffixA(num, rem) => "а"
    case _                                       => "ов"
  s"процент$suffix"

def isApplicableForSuffixA(num: Int, rem: Int): Boolean =
  !(num >= 12 && num <= 14) && rem >= 2 && rem <= 4
