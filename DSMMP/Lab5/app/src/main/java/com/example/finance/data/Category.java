package com.example.finance.data;
import androidx.room.*;
@Entity(tableName="categories") public class Category { @PrimaryKey(autoGenerate=true) public long id; @androidx.annotation.NonNull public String name=""; }
