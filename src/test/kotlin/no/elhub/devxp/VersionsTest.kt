package no.elhub.devxp

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class VersionsTest : DescribeSpec({

    describe("isNonStable function") {

        it("should return false for stable versions") {
            isNonStable("1.0.0") shouldBe false
            isNonStable("2.0.0-RELEASE") shouldBe false
            isNonStable("3.0.0.FINAL") shouldBe false
            isNonStable("4.0.0.GA") shouldBe false
        }

        it("should return true for non-stable versions") {
            isNonStable("1.0.0-beta") shouldBe true
            isNonStable("2.0.0-alpha") shouldBe true
            isNonStable("3.0.0-SNAPSHOT") shouldBe true
            isNonStable("4.0.0-rc.1") shouldBe true
        }
    }
})
