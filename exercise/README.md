## Exercises

***Important that the `exercise` folder is the root of the project.***

This can be opened as a BSP or SBT project in IntelliJ IDEA or VS Code.


### Exercise 1
Add validation to the the CSV parsing
- Id should be Int
- Name should be non-empty
- Quantity should be Int and positive

Use mapN to combine the results of the validations and create a product.

### Exercise 2
Implement a combine Semigroup for product so that it can be used when adding the parsed products from the CSV file to the current intentory.

### Exercise 3
Derive `Show` for `Product` so that it's looks even better in the email body. You should not need to implement it manually.