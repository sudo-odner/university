package com.example.finance.data;
import androidx.room.*;
@Entity(tableName="operations",foreignKeys={@ForeignKey(entity=Account.class,parentColumns="id",childColumns="accountId",onDelete=ForeignKey.CASCADE),@ForeignKey(entity=Category.class,parentColumns="id",childColumns="categoryId",onDelete=ForeignKey.CASCADE)},indices={@Index("accountId"),@Index("categoryId")}) public class Operation { @PrimaryKey(autoGenerate=true) public long id; public long accountId,categoryId,amount,createdAt; @androidx.annotation.NonNull public String note=""; }
