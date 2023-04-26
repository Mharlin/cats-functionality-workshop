package completedDoNotLook

import cats._
import cats.data._
import cats.syntax.all._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import cats.data.Validated.Valid
import cats.data.Validated.Invalid
import scala.util.chaining._

case class Product(id: Int, name: String, quantity: Int)

implicit val productSemigroup: Semigroup[Product] = new Semigroup[Product] {
  def combine(x: Product, y: Product): Product = {
    require(x.id == y.id, "Cannot combine products with different IDs")
    Product(x.id, x.name, x.quantity + y.quantity)
  }
}

trait EmailSender {
  def sendEmail(message: String): Future[Unit]
}

class ConsoleEmailSender extends EmailSender {
  def sendEmail(message: String): Future[Unit] = {
    Future.successful(println(s"Sending email: $message"))
  }
}

type ValidationResult[A] = ValidatedNel[String, A]

def validateId(id: String): ValidationResult[Int] = {
  Try(id.toInt).toEither.leftMap(_ => s"Invalid ID: $id").toValidatedNel
}

def validateName(name: String): ValidationResult[String] = {
  Validated.cond(name.nonEmpty, name, "Name cannot be empty").toValidatedNel
}

def validateQuantity(quantity: String): ValidationResult[Int] = {
  Try(quantity.toInt).toEither.leftMap(_ => s"Invalid quantity: $quantity").toValidatedNel
}

def parseProductCsv(csv: String): List[ValidationResult[Product]] = {
  csv
    .split("\n")
    .toList
    .map { line =>
      val Array(id, name, quantity) = line.split(",")
      (validateId(id), validateName(name), validateQuantity(quantity)).mapN(Product)
    }
}

def updateInventoryFromCsv(
    inventory: List[Product],
    csv: String
): EitherT[Future, NonEmptyList[String], List[Product]] = {
  val parsedProducts = parseProductCsv(csv)
  val validationResult = parsedProducts.sequence

  EitherT(Future.successful(validationResult.toEither))
}

def restockingReport(inventory: List[Product]): Future[List[Product]] = {
  Future.successful(inventory.filter(_.quantity < 10))
}

def sendRestockingEmail(products: List[Product], emailSender: EmailSender): Future[Unit] = {
  emailSender.sendEmail(s"Restock products: $products")
}

val csv = """1,Product A,10
2,Product B,-5
3,Product C,-8"""

val initialInventory = List(
  Product(1, "Product A", 5),
  Product(2, "Product B", 8),
  Product(3, "Product C", 12)
)

def restockProducts(
    initialInventory: List[Product],
    csv: String,
    emailSender: EmailSender
): Future[Unit] = {
  val futureInventory = updateInventoryFromCsv(initialInventory, csv)

  futureInventory
    .flatMap { updatedInventory =>
      (initialInventory ++ updatedInventory)
        .groupBy(_.id)
        .map { case (_, products) => products.reduce(_ |+| _) }
        .toList
        .pipe(combinedInventory => EitherT.right(restockingReport(combinedInventory)))
    }
    .flatMap(restockingProducts => EitherT.right(sendRestockingEmail(restockingProducts, emailSender)))
    .valueOrF(errors => Future.failed(new Exception(errors.toList.mkString("; "))))
}
