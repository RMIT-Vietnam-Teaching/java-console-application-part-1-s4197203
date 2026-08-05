[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/vLkFEkw0)

# ClaimShield - Health Insurance Management System

**COSC3110/3111 Java Programming - Assignment 2 Part 1**

A Java console application for managing health insurance customer profiles, insurance cards, and claims.

## Features

- **Customer Directory**: Add, view, update, and delete customer profiles (PolicyHolders and Dependents)
- **Insurance Cards**: Register cards linked to customers with expiration tracking
- **Claims Processing**: Create claims, add documents, update statuses (New -> Processing -> Done)
- **Search & Filter**: Find customers by name, filter claims by status, sort by date or amount
- **System Statistics**: Aggregated view of customers, cards, claims, and financial totals
- **Data Persistence**: Pipe-delimited text file storage with 17+ sample records per entity

## Author

Nguyen Khanh Nguyen - s4197203

## How to Run

```bash
javac -d out src/model/*.java src/manager/*.java src/ui/*.java src/Main.java
java -cp out Main
```
