package bitronix.tm;

import bitronix.tm.journal.DiskJournal;
import bitronix.tm.journal.Journal;
import bitronix.tm.recovery.Recoverer;
import bitronix.tm.resource.ResourceLoader;
import bitronix.tm.timer.TaskScheduler;
import bitronix.tm.twopc.executor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for all BTM services.
 * <p>The different services available are: TransactionManager, Configuration, Journal, TaskScheduler, ResourceLoader,
 * Recoverer and Executor. They are used in all places of the TM.</p>
 * <p>A shutdown hook is registered when this class is loaded. This hook will perform graceful shutdown.</p>
 * <p>&copy; Bitronix 2005, 2006, 2007</p>
 *
 * @author lorban
 */
public class TransactionManagerServices {

    private final static Logger log = LoggerFactory.getLogger(TransactionManagerServices.class);

    private static BitronixTransactionManager transactionManager;
    private static Configuration configuration;
    private static Journal journal;
    private static TaskScheduler taskScheduler;
    private static ResourceLoader resourceLoader;
    private static Recoverer recoverer;
    private static Executor executor;

    /**
     * Create an initialized transaction manager.
     * @return the transaction manager.
     */
    public synchronized static BitronixTransactionManager getTransactionManager() {
        if (transactionManager == null)
            transactionManager = new BitronixTransactionManager();
        return transactionManager;
    }

    /**
     * Create the configuration of all the components of the transaction manager.
     * @return the global configuration.
     */
    public synchronized static Configuration getConfiguration() {
        if (configuration == null)
            configuration = new Configuration();
        return configuration;
    }

    /**
     * Create the transactions journal.
     * @return the transactions journal.
     */
    public synchronized static Journal getJournal() {
        if (journal == null)
            journal = new DiskJournal();
        return journal;
    }

    /**
     * Create the task scheduler.
     * @return the task scheduler.
     */
    public synchronized static TaskScheduler getTaskScheduler() {
        if (taskScheduler == null) {
            taskScheduler = new TaskScheduler();
            taskScheduler.start();
        }
        return taskScheduler;
    }

    /**
     * Create the resource loader.
     * @return the resource loader.
     */
    public synchronized static ResourceLoader getResourceLoader() {
        if (resourceLoader == null) {
            resourceLoader = new ResourceLoader();
        }
        return resourceLoader;
    }

    /**
     * Create the transaction recoverer.
     * @return the transaction recoverer.
     */
    public synchronized static Recoverer getRecoverer() {
        if (recoverer == null) {
            recoverer = new Recoverer();
        }
        return recoverer;
    }

    /**
     * Create the 2PC executor.
     * @return the 2PC executor.
     */
    public synchronized static Executor getExecutor() {
        if (executor == null) {
            boolean async = getConfiguration().isAsynchronous2Pc();
            if (async) {
                if (log.isDebugEnabled()) log.debug("trying to use ConcurrentExecutor");
                executor = new ConcurrentExecutor();
                if (!executor.isUsable()) {
                    if (log.isDebugEnabled()) log.debug("trying to use BackportConcurrentExecutor");
                    executor = new BackportConcurrentExecutor();
                }
                if (!executor.isUsable()) {
                    if (log.isDebugEnabled()) log.debug("using SimpleAsyncExecutor");
                    executor = new SimpleAsyncExecutor();
                }
            }
            else {
                if (log.isDebugEnabled()) log.debug("using SyncExecutor");
                executor = new SyncExecutor();
            }
        }
        return executor;
    }

    /**
     * Check if the transaction manager has started.
     * @return true if the transaction manager has started.
     */
    public static boolean isTransactionManagerRunning() {
        return transactionManager != null;
    }

    static {
        Runtime.getRuntime().addShutdownHook(new ShutdownHandler());
    }

    private static class ShutdownHandler extends Thread {
        public void run() {
            if (transactionManager != null) {
                if (log.isDebugEnabled()) log.debug("shutdown hook is shutting down Transaction Manager");
                transactionManager.shutdown();
            }
        }
    }

}