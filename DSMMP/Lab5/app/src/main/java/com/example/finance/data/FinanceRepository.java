package com.example.finance.data;
import android.content.Context; import androidx.room.Room; import java.util.concurrent.*;
public class FinanceRepository { public final FinanceDao dao; private final ExecutorService executor=Executors.newSingleThreadExecutor();
 public FinanceRepository(Context c){dao=Room.databaseBuilder(c.getApplicationContext(),FinanceDatabase.class,"finance.db").build().dao();} public void run(Runnable task){executor.execute(task);} }
