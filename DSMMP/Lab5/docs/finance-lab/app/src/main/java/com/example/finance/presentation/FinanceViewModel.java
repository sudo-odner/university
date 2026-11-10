package com.example.finance.presentation;
import android.app.Application; import androidx.lifecycle.*; import com.example.finance.data.*; import java.util.List;
public class FinanceViewModel extends AndroidViewModel { public final FinanceRepository repository; public final LiveData<List<CategoryDetails>> categories; public final LiveData<List<Account>> accounts;
 public FinanceViewModel(Application app){super(app);repository=new FinanceRepository(app);categories=repository.dao.observeCategories();accounts=repository.dao.observeAccounts();} }
