package com.example.finance.data;
import androidx.annotation.NonNull;
import androidx.room.*;
@Entity(tableName="accounts") public class Account {
 @PrimaryKey(autoGenerate=true) public long id;
 @NonNull public String name="";
 @ColumnInfo(defaultValue="0") public long balance=0;
 @ColumnInfo(defaultValue="0") public boolean closed=false;
 @NonNull @ColumnInfo(defaultValue="''") public String expiry="";
}
