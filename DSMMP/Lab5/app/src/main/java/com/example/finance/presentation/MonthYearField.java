package com.example.finance.presentation;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.example.finance.domain.CardExpiry;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

/** Optional card expiry selector: only month and year, never a day. */
final class MonthYearField extends LinearLayout {
 private final TextView valueView; private String value;
 MonthYearField(Context context,String initial){
  super(context);setOrientation(VERTICAL); value=initial==null?"":initial;
  TextView label=FinanceStyle.label(context,"Срок действия карты",14,FinanceStyle.INK,true);addView(label);FinanceStyle.addSpace(this,6);
  valueView=FinanceStyle.action(context,"",false,this::openPicker);valueView.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);valueView.setMinimumHeight(FinanceStyle.dp(context,52));addView(valueView);
  FinanceStyle.addSpace(this,5);TextView hint=FinanceStyle.label(context,"Необязательно · выберите месяц и год",12,FinanceStyle.MUTED,false);addView(hint);render();
 }
 String getValue(){return value;}
 private void render(){valueView.setText(value.isEmpty()?"Выбрать срок действия  ›":value.substring(5)+" / "+value.substring(0,4)+"   ·   Изменить  ›");}
 private void openPicker(){
  Calendar now=Calendar.getInstance();int currentYear=now.get(Calendar.YEAR),currentMonth=now.get(Calendar.MONTH)+1;
  int selectedYear=currentYear,selectedMonth=currentMonth;
  if(!value.isEmpty() && CardExpiry.canSave(value,"",currentYear,currentMonth)){selectedYear=Integer.parseInt(value.substring(0,4));selectedMonth=Integer.parseInt(value.substring(5));}
  LinearLayout pickers=new LinearLayout(getContext());pickers.setGravity(Gravity.CENTER);int pad=FinanceStyle.dp(getContext(),16);pickers.setPadding(pad,pad,pad,pad);
  NumberPicker month=new NumberPicker(getContext()),year=new NumberPicker(getContext());
  String[] months=new DateFormatSymbols(new Locale("ru")).getShortMonths();String[] display=new String[12];for(int i=0;i<12;i++)display[i]=months[i].replace(".","");
  month.setMinValue(1);month.setMaxValue(12);month.setDisplayedValues(display);month.setWrapSelectorWheel(false);
  year.setMinValue(currentYear);year.setMaxValue(Math.max(currentYear+30,selectedYear));year.setWrapSelectorWheel(false);year.setValue(selectedYear);month.setValue(selectedMonth);
  pickers.addView(month,new LinearLayout.LayoutParams(0,-2,1));pickers.addView(year,new LinearLayout.LayoutParams(0,-2,1));
  AlertDialog dialog=new AlertDialog.Builder(getContext()).setTitle("Месяц и год окончания").setView(pickers).setNegativeButton("Отмена",null).setNeutralButton("Очистить",(d,w)->{value="";render();}).setPositiveButton("Выбрать",null).create();
  dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String selected=CardExpiry.format(year.getValue(),month.getValue());Calendar c=Calendar.getInstance();if(!CardExpiry.canSave(selected,"",c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1)){android.widget.Toast.makeText(getContext(),"Выберите текущий или будущий месяц",android.widget.Toast.LENGTH_LONG).show();return;}value=selected;render();dialog.dismiss();}));dialog.show();
 }
}
