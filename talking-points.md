### Intro and agenda

Imports to always include

```scala
import cats._
import cats.data._
import cats.syntax.all._
```

### Intro to cats - type classes
- Functor
  - map
- Applicative
  - pure
  - ap
- Monad
  - flatMap
  - flatten
  - tailRecM
- Semigroup
  - combine
- Monoid
  - empty

Go throught the relationship and what they implement and how that can help with chaining.

### Hidden gems in cats
`.lift` - to work with the value of Option, Either. Will return a success or failure.
Example

```scala
import cats.syntax._

val double: Int => Int = _ * 2
val optInt: Option[Int] = Some(3)

// Using Cats syntax to lift the double function and apply it to the Option
val doubledOptInt: Option[Int] = double.lift(optInt)
```

`flattenOption` to filter out None values from a List and Map of Options

`tapError` - to log the error and return the original value
`onError` - to log the error and return a default value

`mapN` - to apply multiple wrapped parameters to a function

`asRight.asFuture` - to convert Either to Future
  Instead of writing `Future.successful(Right(value))` we can write `value.asRight.asFuture`

`.sequence` - to convert a collection of F[_] to F[Collection[_]]

`.traverse` is a combination of `map` and `sequence`

- `.flatTraverse` when ending up with too much wrapping i.e. `Future[Option[Option[A]]` or `Future[List[List[A]]]` — a bit awkward to process`

```scala
import cats.syntax.all._
  
lazy val valueOpt: Option[Int] = { /* ... */ ??? }
def compute(value: Int): Future[Option[Int]] = { /* ... */ ??? }
def computeOverValue: Future[Option[Option[Int]]] = valueOpt.traverse(compute) // has type Future[Option[Option[Int]]], not good

// could be solved with
def computeOverValue2: Future[Option[Int]] = valueOpt.traverse(compute).map(_.flatten)

// even nicer with
def computeOverValue3: Future[Option[Int]] = valueOpt.flatTraverse(compute)
```

`OptionT` - Related to EitherT. OptionT is for working with `F[Option[A]]` and can easily be converted to EitherT

## Combinations
How to combine partial functions

```scala
// Define three partial functions
val positiveSqrt: PartialFunction[Double, String] = {
  case x if x > 0 => s"Sqrt of $x is ${math.sqrt(x)}"
}

val negativeSqrt: PartialFunction[Double, String] = {
  case x if x < 0 => s"Sqrt of $x is not defined (imaginary number)"
}

val zeroSqrt: PartialFunction[Double, String] = {
  case x if x == 0 => s"Sqrt of $x is 0"
}

// Combine the partial functions using `orElse`
val sqrtDescription = positiveSqrt.orElse(negativeSqrt).orElse(zeroSqrt)

// Test the combined partial function on different input values
val numbers = List(-4, -1, 0, 1, 4, 9)
val results = numbers.map(sqrtDescription)
println(results)
// Output:
// List(
//   "Sqrt of -4.0 is not defined (imaginary number)",
//   "Sqrt of -1.0 is not defined (imaginary number)",
//   "Sqrt of 0.0 is 0",
//   "Sqrt of 1.0 is 1.0",
//   "Sqrt of 4.0 is 2.0",
//   "Sqrt of 9.0 is 3.0"
// )
```

TODO: Add validated to the slides

## Exercise
- Print error when restocking products fails
- Implement Semigroup for combining products and getting the right count
- Implement implicit Show for Product to get nice printout
- 

TODO: make use of traverse of sequence and maybe lift as well
      add the email sender to the solution again
