package mayeul.language

import org.scalatest.{FunSpec, Matchers}

/**
  * [0]x.y
  * If '0' prefix -> padding with 0s. Otherwise padding with spaces.
  * y: exact number of decimals.
  * x: minimal number of characters (including '.' and decimals). This involves potentially padding.
  * If the value is big enough it will require more than x characters.
  */
class FormatTest extends FunSpec with Matchers {
  describe(classOf[FormatTest].getName) {
    val q: Double = 80.0 / 7
    it("should print with 2 decimals") {
      f"$q%1.2f" should be("11.43")
    }
    it("should print with 2 decimals and 2 padded spaces") {
      f"$q%7.2f" should be("  11.43")
    }
    it("should print with 2 decimals and 2 padded 0s") {
      f"$q%07.2f" should be("0011.43")
    }
  }
}
