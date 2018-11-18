package org.comic_con.museum.fcb.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.Closeable;

// TODO Add support for S3 "transactions" (rollback on failure)
@Service
public class TransactionWrapper {
    private static final Logger LOG = LoggerFactory.getLogger("transaction");
    
    public class Transaction implements Closeable {
        private final TransactionStatus status;
        private boolean completed;
        
        private Transaction(TransactionStatus status) {
            this.status = status;
            this.completed = false;
        }
        
        public boolean isCompleted() {
            // just because we don't have it listed as complete doesn't mean that it
            // hasn't been completed with the lower-level interface
            return this.completed || this.status.isCompleted();
        }
    
        public void commit() {
            LOG.info("Committing transaction");
            this.completed = true;
            TransactionWrapper.this.transactionManager.commit(this.status);
        }
    
        public void rollback() {
            LOG.info("Rolling back transaction");
            this.completed = true;
            TransactionWrapper.this.transactionManager.rollback(this.status);
        }
    
        @Override
        public void close() {
            if (!this.isCompleted()) this.rollback();
        }
    }
    
    private final DataSourceTransactionManager transactionManager;
    
    @Autowired
    public TransactionWrapper(DataSourceTransactionManager tm) {
        this.transactionManager = tm;
    }
    
    public Transaction start() {
        LOG.info("Starting transaction");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        return new Transaction(transactionManager.getTransaction(def));
    }
    
    public DataSourceTransactionManager getTransactionManager() {
        return this.transactionManager;
    }
}
