package completedDoNotLook

import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.data.NonEmptyList
import scala.concurrent.Future
import scala.collection.mutable.ArrayBuffer

class ProductRestockSpec extends AsyncFlatSpec with Matchers {
  class TestEmailSender extends EmailSender {
    val sentEmails: ArrayBuffer[String] = ArrayBuffer.empty[String]

    def sendEmail(message: String): Future[Unit] = {
      sentEmails += message
      Future.successful(())
    }
  }
  
  val testEmailSender = new TestEmailSender

  "restockProducts" should "update the inventory and send an email for restocking" in {
    val expectedUpdatedInventory = List(
      Product(1, "Product A", 15),
      Product(2, "Product B", 3),
      Product(3, "Product C", 4)
    )

    restockProducts(initialInventory, csv, testEmailSender).map { _ =>
      assert(true) // If the future is successful, the test passes

      // Check the email body
      testEmailSender.sentEmails should have length 1
      testEmailSender.sentEmails.head should include("Restock products: List(Product(2,Product B,3), Product(3,Product C,4))")
    }
  }

  it should "fail with a meaningful error message if the CSV contains invalid data" in {
    val invalidCsv = """1,Product A,10
2,Product B,not_a_number
3,Product C,8"""

    recoverToExceptionIf[Exception] {
      restockProducts(initialInventory, invalidCsv, testEmailSender)
    }.map { exception =>
      exception.getMessage should include("Invalid quantity: not_a_number")
    }
  }

  "validateId" should "validate a valid ID" in {
    validateId("1").isValid shouldBe true
  }

  it should "invalidate an invalid ID" in {
    validateId("not_an_id").isInvalid shouldBe true
  }

  "validateName" should "validate a non-empty name" in {
    validateName("Product A").isValid shouldBe true
  }

  it should "invalidate an empty name" in {
    validateName("").isInvalid shouldBe true
  }

  "validateQuantity" should "validate a valid quantity" in {
    validateQuantity("10").isValid shouldBe true
  }

  it should "invalidate an invalid quantity" in {
    validateQuantity("not_a_number").isInvalid shouldBe true
  }

}
