package com.example.finance.domain;
import org.junit.Test; import static org.junit.Assert.*;
public class AccountRulesTest {
 @Test public void exactBalanceCanBeSpent(){assertEquals(0,AccountRules.debit(100,100));}
 @Test public void transferCalculationsPreserveMoney(){assertEquals(200,AccountRules.debit(200,50)+AccountRules.credit(0,50));}
 @Test(expected=IllegalArgumentException.class) public void cannotSpendMoreThanBalance(){AccountRules.debit(99,100);}
 @Test(expected=IllegalArgumentException.class) public void cannotTransferZero(){AccountRules.debit(100,0);}
 @Test(expected=IllegalArgumentException.class) public void cannotOverflow(){AccountRules.credit(Long.MAX_VALUE,1);}
}
