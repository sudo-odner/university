package com.example.finance.data;

import static org.junit.Assert.*;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AccountLedgerTest {
 private FinanceDatabase db; private FinanceDao dao;
 @Before public void setup(){db=Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),FinanceDatabase.class).allowMainThreadQueries().build();dao=db.dao();}
 @After public void close(){db.close();}
 private long account(String name,long balance){Account a=new Account();a.name=name;a.balance=balance;return dao.addAccount(a);}
 @Test public void closeTransfersAllMoneyAndKeepsHistory(){long from=account("Старая",1000),to=account("Новая",250);dao.transfer(from,to,1000,true);assertTrue(dao.account(from).closed);assertEquals(0,dao.account(from).balance);assertEquals(1250,dao.account(to).balance);assertEquals(1,dao.movements(from).size());}
 @Test public void rejectedTransferDoesNotChangeAccounts(){long from=account("А",100),to=account("Б",50);try{dao.transfer(from,to,101,true);fail();}catch(IllegalArgumentException expected){}assertEquals(100,dao.account(from).balance);assertEquals(50,dao.account(to).balance);assertFalse(dao.account(from).closed);assertTrue(dao.movements(from).isEmpty());}
 @Test public void closedAccountRejectsNewTransactions(){long from=account("А",0);dao.closeEmpty(from);try{dao.deposit(from,10);fail();}catch(IllegalStateException expected){}assertEquals(0,dao.account(from).balance);}
 @Test public void expenseAndRefundUpdateBalance(){long id=account("А",1000);Category c=new Category();c.name="Еда";long category=dao.addCategory(c);Operation o=new Operation();o.accountId=id;o.categoryId=category;o.amount=200;dao.spend(o);assertEquals(800,dao.account(id).balance);// The transaction inserted one operation into a fresh database.
 dao.removeExpense(1);assertEquals(1000,dao.account(id).balance);}
}
