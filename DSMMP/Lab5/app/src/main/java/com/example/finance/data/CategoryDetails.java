package com.example.finance.data;
import androidx.room.*; import java.util.List;
public class CategoryDetails { @Embedded public Category category; @Relation(parentColumn="id",entityColumn="categoryId") public List<Operation> operations; @Relation(parentColumn="id",entityColumn="categoryId") public List<Budget> budgets; }
