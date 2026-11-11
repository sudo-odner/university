package com.example.finance.data;
import androidx.room.*;
@Entity(tableName="budgets",foreignKeys=@ForeignKey(entity=Category.class,parentColumns="id",childColumns="categoryId",onDelete=ForeignKey.CASCADE),indices=@Index(value="categoryId",unique=true)) public class Budget { @PrimaryKey(autoGenerate=true) public long id; public long categoryId,amount; public int warningPercent=80; }
