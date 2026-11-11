package com.example.finance.domain;
import static org.junit.Assert.*;
import org.junit.Test;
public class CardExpiryTest {
 @Test public void allowsCurrentAndFutureMonths(){assertTrue(CardExpiry.canSave("2026-11","",2026,11));assertTrue(CardExpiry.canSave("2027-01","",2026,11));}
 @Test public void rejectsPastAndInvalidMonths(){assertFalse(CardExpiry.canSave("2026-10","",2026,11));assertFalse(CardExpiry.canSave("2026-00","",2026,11));assertFalse(CardExpiry.canSave("2025-12","",2026,11));assertFalse(CardExpiry.canSave("2026-13","",2026,11));assertFalse(CardExpiry.canSave("26-11","",2026,11));}
 @Test public void permitsEmptyAndExistingExpiredDate(){assertTrue(CardExpiry.canSave("","",2026,11));assertTrue(CardExpiry.canSave("2020-01","2020-01",2026,11));}
}
