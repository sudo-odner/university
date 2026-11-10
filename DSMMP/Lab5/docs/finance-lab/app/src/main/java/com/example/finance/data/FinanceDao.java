package com.example.finance.data;
import androidx.room.*; import androidx.lifecycle.LiveData; import java.util.List;
@Dao public interface FinanceDao {
 @Transaction @Query("SELECT * FROM categories ORDER BY name") LiveData<List<CategoryDetails>> observeCategories();
 @Query("SELECT * FROM accounts ORDER BY name") LiveData<List<Account>> observeAccounts();
 @Insert long addAccount(Account a); @Insert long addCategory(Category c); @Insert long addOperation(Operation o); @Insert(onConflict=OnConflictStrategy.REPLACE) void putBudget(Budget b);
 @Update void updateAccount(Account a); @Update void updateCategory(Category c); @Update void updateOperation(Operation o);
 @Delete void deleteAccount(Account a); @Delete void deleteCategory(Category c); @Delete void deleteOperation(Operation o);
}
