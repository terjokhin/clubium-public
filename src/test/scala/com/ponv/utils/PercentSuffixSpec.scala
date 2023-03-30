package com.ponv.utils

import com.ponv.utils.getPercentSuffix

class PercentSuffixSpec extends munit.FunSuite:

  final case class TestCase(name: String, num: Int, expectedSuffix: String)

  val testCases: List[TestCase] = List(
    TestCase("Return correct suffix for 1", 1, "процент"),
    TestCase("Return correct suffix for 2", 2, "процента"),
    TestCase("Return correct suffix for 3", 3, "процента"),
    TestCase("Return correct suffix for 4", 4, "процента"),
    TestCase("Return correct suffix for 5", 5, "процентов"),
    TestCase("Return correct suffix for 11", 11, "процентов"),
    TestCase("Return correct suffix for 12", 12, "процентов"),
    TestCase("Return correct suffix for 13", 13, "процентов"),
    TestCase("Return correct suffix for 14", 14, "процентов"),
    TestCase("Return correct suffix for 21", 21, "процент"),
    TestCase("Return correct suffix for 22", 22, "процента"),
    TestCase("Return correct suffix for 33", 33, "процента"),
    TestCase("Return correct suffix for 44", 44, "процента"),
    TestCase("Return correct suffix for 55", 55, "процентов")
  )

  testCases.foreach { testCase =>
    test(testCase.name) { assert(getPercentSuffix(testCase.num) == testCase.expectedSuffix) }
  }
