package pt.unl.fct.di.adc.firstwebapp.util;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Transaction;
import java.util.function.Function;
import java.util.logging.Logger;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorCode;

public class DB {
    
    private static final Logger LOG = Logger.getLogger(DB.class.getName());

    /**
     * Executes the provided database logic inside a managed transaction.
     * Automatically handles commits, rollbacks, and generic error catching.
     */
    public static <T> Result<T> executeInTransaction(Datastore datastore, Function<Transaction, Result<T>> action) {
        Transaction txn = datastore.newTransaction();
        try {
            Result<T> result = action.apply(txn);
            txn.commit();
            return result;
        } catch (Exception e) {
            LOG.severe("Transaction failed: " + e.getMessage());
            return Result.failure(ErrorCode.FORBIDDEN); 
        } finally {
            if (txn != null && txn.isActive()) {
                txn.rollback();
            }
        }
    }
}
