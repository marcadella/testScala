package mayeul.language

import org.scalatest.{FunSpec, Matchers}

/**
  * Cf https://regex101.com/
  * (...) -> capture that can be pattern matched
  * [...] -> range
  * ? makes the preceding lazy (without it is greedy)
  */
class RegexTest extends FunSpec with Matchers {
  describe(classOf[RegexTest].getName) {
    val regex = "^(.+?)[\\W_]?(r1|r2)\\.fastq$".r
    it("should not match") {
      val notMatch = "r3.fastq"
      notMatch.matches(regex.toString) should be(false)
    }
    it("should match") {
      val shouldMatch = "bla_r1.fastq"
      shouldMatch.matches(regex.toString) should be(true)
    }
    it("should extract the captures") {
      val shouldMatch = "bla_r1.fastq"
      shouldMatch match {
        case regex(fileNamePrefix, fileNameSuffix) =>
          fileNamePrefix should be("bla")
          fileNameSuffix should be("r1")
      }
    }
    it("should demonstrate the use of @") {
      val shouldMatch = "bla_r1.fastq"
      (shouldMatch match {
        case fn @ regex(fileNamePrefix, fileNameSuffix) =>
          fileNamePrefix should be("bla")
          fileNameSuffix should be("r1")
          fn.length //@ give you access to the string being pattern-matched as well
      }) should be(12)
    }
  }
}
