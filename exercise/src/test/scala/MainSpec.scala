package restock

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.collection.mutable.ArrayBuffer

class ProductRestockingSpec extends AnyFlatSpec with Matchers {
  class TestEmailSender extends EmailSender {
    val sentEmails: ArrayBuffer[String] = ArrayBuffer.empty[String]

    def sendEmail(message: String): Future[Unit] = {
      sentEmails += message
      Future.successful(())
    }
  }

  "parseCsvLine" should "parse a CSV line correctly" in {
    parseCsvLine("1,Product A,10") shouldEqual Some(Product(1, "Product A", 10))
  }

  it should "return None for an invalid CSV line" in {
    parseCsvLine("1,Product A,invalid") shouldBe None
  }

  "updateInventoryFromCsv" should "update inventory from a CSV string" in {
    val inventory = List(Product(1, "Product A", 5), Product(2, "Product B", 8))
    val csv = "1,Product A,5\n2,Product B,7"
    val expectedInventory =
      List(Product(1, "Product A", 10), Product(2, "Product B", 15))

    val result = Await.result(updateInventoryFromCsv(inventory, csv), 1.second)
    result should contain theSameElementsAs expectedInventory
  }

  "restockingReport" should "return products with a quantity less than 10" in {
    val inventory = List(
      Product(1, "Product A", 5),
      Product(2, "Product B", 15),
      Product(3, "Product C", 8)
    )
    val expectedReport =
      List(Product(1, "Product A", 5), Product(3, "Product C", 8))

    val result = Await.result(restockingReport(inventory), 1.second)
    result should contain theSameElementsAs expectedReport
  }

  "restockProducts" should "send the expected restocking email" in {
    val emailSender = new TestEmailSender

    val initialInventory = List(
      Product(1, "Product A", 5),
      Product(2, "Product B", 8),
      Product(3, "Product C", 12)
    )

    val csv = """1,Product A,5
  2,Product B,1
  3,Product C,1"""

    Await.result(restockProducts(initialInventory, csv, emailSender), 1.second)

    emailSender.sentEmails should have size 1
    val body = emailSender.sentEmails.head

    body should include("Product A")
    body should include("Product B")
  }
}
