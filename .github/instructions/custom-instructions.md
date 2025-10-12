---
applyTo: '**'
---
# Instruction for Copilot:
You are assisting with code comments and documentation.
Follow these rules strictly:

1. **Do not add comments** if:
   - The class, method, or variable name is already self-explanatory or clearly conveys its purpose.
   - The logic is simple and easy to understand from reading the code itself.

2. **Only add comments** if:
   - The code involves complex logic, algorithms, or business rules that are not obvious at first glance.
   - There are side effects, assumptions, or performance considerations that may not be apparent.
   - External dependencies, edge cases, or unusual design decisions are present.

3. **Be concise and purposeful.**
   - Use clear, short comments that explain *why* the code exists, not *what* it does.

4. **Do not restate the code.**
   - Avoid comments like “increments count by one” when the line is `count++`.

