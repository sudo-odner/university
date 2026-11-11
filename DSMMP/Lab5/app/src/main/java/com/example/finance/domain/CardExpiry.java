package com.example.finance.domain;

import java.util.Calendar;
import java.util.Locale;

public final class CardExpiry {
 private CardExpiry() {}
 public static boolean canSave(String value, String previous, int year, int month) {
  if(value == null) return false;
  if(value.isEmpty() || value.equals(previous)) return true;
  if(!value.matches("[0-9]{4}-[0-9]{2}")) return false;
  int y=Integer.parseInt(value.substring(0,4)), m=Integer.parseInt(value.substring(5));
  return m>=1 && m<=12 && (y>year || (y==year && m>=month));
 }
 public static void check(String value,String previous){Calendar c=Calendar.getInstance();if(!canSave(value,previous,c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1))throw new IllegalArgumentException("Выберите текущий или будущий месяц");}
 public static String format(int year,int month){return String.format(Locale.ROOT,"%04d-%02d",year,month);}
}
