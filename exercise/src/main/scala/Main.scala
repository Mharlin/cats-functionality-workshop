package restock

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import scala.concurrent.Await
import cats._
import cats.data._
import cats.syntax.all._

@main def main: Unit =
  val emailSender = new EmailSender {
    override def sendEmail(message: String): Future[Unit] = {
      Future.successful(println(s"Sending email: $message"))
    }
  }

  Await.result(
    restockProducts(initialInventory, csv, emailSender),
    scala.concurrent.duration.Duration.Inf
  )

case class Product(id: Int, name: String, quantity: Int)

trait EmailSender {
  def sendEmail(message: String): Future[Unit]
}

def parseCsvLine(line: String): Option[Product] = {
  val Array(id, name, quantity) = line.split(",")

  Try((id.toInt, name, quantity.toInt)) match {
    case Success((validId, validName, validQuantity)) =>
      Some(Product(validId, validName, validQuantity))
    case Failure(_) => None
  }
}

def updateInventoryFromCsv(
    inventory: List[Product],
    csv: String
): Future[List[Product]] = {
  val parsedProducts = csv
    .split("\n")
    .toList
    .flatMap(parseCsvLine)

  val combinedInventory = (inventory ++ parsedProducts)
    .groupBy(_.id)
    .map { case (_, products) =>
      products.reduce((x, y) => Product(x.id, x.name, x.quantity + y.quantity))
    }
    .toList

  Future.successful(combinedInventory)
}

def restockingReport(inventory: List[Product]): Future[List[Product]] = {
  Future.successful(inventory.filter(_.quantity <= 10))
}

def sendRestockingEmail(
    emailSender: EmailSender,
    products: List[Product]
): Future[Unit] = {
  val body = s"Sending restocking email for products: $products"

  emailSender.sendEmail(body)
}

val csv = """1,Product A,10
2,Product B,5
3,Product C,8"""

val initialInventory = List(
  Product(1, "Product A", 5),
  Product(2, "Product B", 8),
  Product(3, "Product C", 12)
)

def restockProducts(
    inventory: List[Product],
    csv: String,
    emailSender: EmailSender
): Future[Unit] = {
  updateInventoryFromCsv(inventory, csv)
    .flatMap(restockingReport)
    .flatMap(products => sendRestockingEmail(emailSender, products))
}


type ValidationResult[A] = ValidatedNel[String, A]

def validateId(id: String): ValidationResult[Int] = ???

def validateName(name: String): ValidationResult[String] = ???

def validateQuantity(quantity: String): ValidationResult[Int] = ???

def parseProductCsv(csv: String): List[ValidationResult[Product]] = ???

def updateInventoryFromCsvUpdated(
    inventory: List[Product],
    csv: String
): EitherT[Future, NonEmptyList[String], List[Product]] = ???

def restockingReportUpdated(inventory: List[Product]): Future[List[Product]] = ???

def sendRestockingEmail(products: List[Product], emailSender: EmailSender): Future[Unit] = ???

def restockProductsUpdated(
    initialInventory: List[Product],
    csv: String,
    emailSender: EmailSender
): Future[Unit] = ???