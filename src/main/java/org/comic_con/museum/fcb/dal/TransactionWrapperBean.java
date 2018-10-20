package org.comic_con.museum.fcb.dal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Component
public class TransactionWrapperBean {
    private final DataSourceTransactionManager transactionManager;
    
    @Autowired
    public TransactionWrapperBean(DataSourceTransactionManager tm) {
        this.transactionManager = tm;
    }
    
    public void run(BiConsumer<DataSourceTransactionManager, TransactionStatus> block) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            block.accept(transactionManager, status);
        } catch (SQLException e) {
            
        }
    }
}
