package com.example.finance.data;
import androidx.room.*;
@Database(entities={Account.class,Category.class,Operation.class,Budget.class},version=1,exportSchema=false) public abstract class FinanceDatabase extends RoomDatabase { public abstract FinanceDao dao(); }
