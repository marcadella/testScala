package mayeul.utils

import org.scalatest.{FunSpec, Matchers}

class StringUtilsTest extends FunSpec with Matchers {
  describe(classOf[StringUtilsTest].getName) {
    val str = "abcdefghij"
    it("Should not truncate") {
      StringUtils.truncateString(str, 10) should be(str)
    }
    it("Should truncate properly") {
      StringUtils.truncateString(str, 9) should be("abcdef...")
      StringUtils.truncateString(str, 8) should be("abcde...")
      StringUtils.truncateString(str, 7) should be("abcd...")
      StringUtils.truncateString(str, 3) should be("...")
      StringUtils.truncateString(str, 2) should be("..")
      StringUtils.truncateString(str, 1) should be(".")
      StringUtils.truncateString(str, 0) should be("")
    }
  }
}
