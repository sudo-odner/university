package com.example.finance.data;
import androidx.room.*; import androidx.lifecycle.LiveData; import java.util.List;
import com.example.finance.domain.AccountRules; import com.example.finance.domain.CardExpiry;
@Dao public interface FinanceDao {
 @Transaction @Query("SELECT * FROM categories ORDER BY name") LiveData<List<CategoryDetails>> observeCategories();
 @Query("SELECT * FROM accounts ORDER BY closed, name") LiveData<List<Account>> observeAccounts();
 @Query("SELECT * FROM accounts WHERE id=:id") Account account(long id);
 @Query("SELECT * FROM operations WHERE id=:id") Operation operation(long id);
 @Query("SELECT * FROM account_movements WHERE sourceId=:id OR destinationId=:id ORDER BY createdAt DESC") List<AccountMovement> movements(long id);
 @Query("SELECT COUNT(*) FROM operations WHERE categoryId=:id") int categoryOperationCount(long id);
 @Query("SELECT COUNT(*) FROM categories WHERE id=:id") int categoryCount(long id);
 @Insert long addAccount(Account a); @Insert long addCategory(Category c); @Insert long addOperation(Operation o); @Insert long addMovement(AccountMovement m); @Insert(onConflict=OnConflictStrategy.REPLACE) void putBudget(Budget b);
 @Update void updateAccount(Account a); @Update void updateCategory(Category c); @Update void updateOperation(Operation o);
 @Delete void deleteCategory(Category c); @Delete void deleteOperation(Operation o);
 default Account active(long id) { Account a=account(id); if(a==null || a.closed) throw new IllegalStateException("Счёт уже закрыт или недоступен"); return a; }
 @Transaction default void createAccount(Account a) { CardExpiry.check(a.expiry, ""); if(a.name.trim().isEmpty())throw new IllegalArgumentException("Введите название счёта"); addAccount(a); }
 @Transaction default void editAccount(long id,String name,String expiry) { Account a=active(id);if(name.trim().isEmpty())throw new IllegalArgumentException("Введите название счёта");CardExpiry.check(expiry,a.expiry);a.name=name;a.expiry=expiry;updateAccount(a); }
 @Transaction default void deposit(long id,long amount) { Account a=active(id); a.balance=AccountRules.credit(a.balance,amount);updateAccount(a);AccountMovement m=new AccountMovement();m.destinationId=id;m.amount=amount;m.createdAt=System.currentTimeMillis();m.type="Пополнение";addMovement(m); }
 @Transaction default void transfer(long from,long to,long amount,boolean close) { if(from==to)throw new IllegalArgumentException("Выберите другой счёт");Account a=active(from), b=active(to); long left=AccountRules.debit(a.balance,amount);long next=AccountRules.credit(b.balance,amount);if(close && left!=0)throw new IllegalArgumentException("Для закрытия переведите весь остаток");a.balance=left;a.closed=close;b.balance=next;updateAccount(a);updateAccount(b);AccountMovement m=new AccountMovement();m.sourceId=from;m.destinationId=to;m.amount=amount;m.createdAt=System.currentTimeMillis();m.type=close?"Закрытие счёта":"Перевод";addMovement(m); }
 @Transaction default void closeEmpty(long id) { Account a=active(id); if(a.balance!=0)throw new IllegalArgumentException("Сначала переведите оставшиеся средства");a.closed=true;updateAccount(a); }
 @Transaction default void spend(Operation o) { Account a=active(o.accountId);if(categoryCount(o.categoryId)==0)throw new IllegalArgumentException("Категория больше не существует");a.balance=AccountRules.debit(a.balance,o.amount);o.appliedToBalance=true;updateAccount(a);addOperation(o); }
 @Transaction default void editExpense(long id,long amount,String note) { Operation o=operation(id);if(o==null)throw new IllegalArgumentException("Операция уже удалена");if(!o.appliedToBalance)throw new IllegalArgumentException("Старые операции без учёта остатка нельзя редактировать");Account a=active(o.accountId);long restored=AccountRules.credit(a.balance,o.amount);a.balance=AccountRules.debit(restored,amount);o.amount=amount;o.note=note;updateAccount(a);updateOperation(o); }
 @Transaction default void removeExpense(long id) { Operation o=operation(id);if(o==null)throw new IllegalArgumentException("Операция уже удалена"); if(o.appliedToBalance){Account a=active(o.accountId);a.balance=AccountRules.credit(a.balance,o.amount);updateAccount(a);}deleteOperation(o); }
 @Transaction default void removeCategory(Category c) { if(categoryOperationCount(c.id)>0)throw new IllegalStateException("В категории есть операции. Удалите или перенесите их перед удалением категории.");deleteCategory(c); }
}
