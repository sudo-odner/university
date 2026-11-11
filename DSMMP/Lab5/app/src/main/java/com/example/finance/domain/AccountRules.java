package com.example.finance.domain;

/** Rules for the local ledger. Amounts are integer kopecks. */
public final class AccountRules {
 private AccountRules() {}
 public static void checkAmount(long amount) { if(amount<=0) throw new IllegalArgumentException("Сумма должна быть больше нуля"); }
 public static long debit(long balance,long amount) { checkAmount(amount); if(amount>balance) throw new IllegalArgumentException("Недостаточно средств на счёте"); return balance-amount; }
 public static long credit(long balance,long amount) { checkAmount(amount); try { return Math.addExact(balance,amount); } catch(ArithmeticException e) { throw new IllegalArgumentException("Слишком большая сумма"); } }
}
