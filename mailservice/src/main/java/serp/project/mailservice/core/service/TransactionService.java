/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.function.Supplier;

/**
 * Domain service for transaction management.
 * Provides programmatic transaction control for use cases.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final PlatformTransactionManager transactionManager;

    /**
     * Executes operation within a transaction.
     * Commits on success, rolls back on exception.
     *
     * @param operation Operation to execute
     * @param <T> Return type
     * @return Operation result
     * @throws RuntimeException if operation fails
     */
    public <T> T executeInTransaction(Supplier<T> operation) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        TransactionStatus status = transactionManager.getTransaction(def);
        
        try {
            log.debug("Starting transaction");
            
            T result = operation.get();
            
            transactionManager.commit(status);
            log.debug("Transaction committed successfully");
            
            return result;
            
        } catch (Exception e) {
            log.error("Transaction failed, rolling back", e);
            
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
                log.debug("Transaction rolled back");
            }
            
            throw e;
        }
    }

    /**
     * Executes void operation within a transaction.
     *
     * @param operation Operation to execute
     */
    public void executeInTransactionVoid(Runnable operation) {
        executeInTransaction(() -> {
            operation.run();
            return null;
        });
    }

    /**
     * Executes operation within a new transaction.
     * Creates new transaction even if one already exists.
     *
     * @param operation Operation to execute
     * @param <T> Return type
     * @return Operation result
     */
    public <T> T executeInNewTransaction(Supplier<T> operation) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        
        TransactionStatus status = transactionManager.getTransaction(def);
        
        try {
            log.debug("Starting new transaction");
            
            T result = operation.get();
            
            transactionManager.commit(status);
            log.debug("New transaction committed successfully");
            
            return result;
            
        } catch (Exception e) {
            log.error("New transaction failed, rolling back", e);
            
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
                log.debug("New transaction rolled back");
            }
            
            throw e;
        }
    }

    /**
     * Executes operation with read-only transaction.
     * Optimized for read operations.
     *
     * @param operation Operation to execute
     * @param <T> Return type
     * @return Operation result
     */
    public <T> T executeInReadOnlyTransaction(Supplier<T> operation) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setReadOnly(true);
        
        TransactionStatus status = transactionManager.getTransaction(def);
        
        try {
            log.debug("Starting read-only transaction");
            
            T result = operation.get();
            
            transactionManager.commit(status);
            log.debug("Read-only transaction completed");
            
            return result;
            
        } catch (Exception e) {
            log.error("Read-only transaction failed", e);
            
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
            }
            
            throw e;
        }
    }
}
