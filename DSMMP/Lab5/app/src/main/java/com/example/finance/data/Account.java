package com.example.finance.data;
import androidx.room.*;
@Entity(tableName="accounts") public class Account { @PrimaryKey(autoGenerate=true) public long id; @androidx.annotation.NonNull public String name=""; }
