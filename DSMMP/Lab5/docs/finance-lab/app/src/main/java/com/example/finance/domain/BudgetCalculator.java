package com.example.finance.domain;
public final class BudgetCalculator {
 public enum Level { NONE, NORMAL, WARNING, EXCEEDED }
 public static Level level(long spent,long limit,int threshold) { if(limit<=0)return Level.NONE; if(spent>limit)return Level.EXCEEDED; if((double)spent/limit*100>=threshold)return Level.WARNING; return Level.NORMAL; }
 public static long remaining(long spent,long limit){return limit-spent;}
}
