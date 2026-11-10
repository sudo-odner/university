package com.example.finance.domain;
import org.junit.Test; import static org.junit.Assert.*;
public class BudgetCalculatorTest { @Test public void levels(){assertEquals(BudgetCalculator.Level.NONE,BudgetCalculator.level(10,0,80)); assertEquals(BudgetCalculator.Level.NORMAL,BudgetCalculator.level(79,100,80)); assertEquals(BudgetCalculator.Level.WARNING,BudgetCalculator.level(80,100,80)); assertEquals(BudgetCalculator.Level.EXCEEDED,BudgetCalculator.level(101,100,80)); assertEquals(-1,BudgetCalculator.remaining(101,100));} }
