package com.banking.app.policy;

import com.banking.app.model.Account;
import java.math.BigDecimal;

public interface TransactionPolicy {
    void validateWithdraw(Account account, BigDecimal amount);

    void validateDeposit(Account account, BigDecimal amount);

    void validateTransfer(Account source, Account destination, BigDecimal amount);
}
