package tempest.transaction;

import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Stack;
import java.util.logging.Logger;

public class TransactionalContext {

    private static String PERSISTENCE_UNIT_NAME = "Waterbit-PU";

    private static EntityManagerFactory emFactory;

    private static class InternalContext {

        private Stack<EntityManager> ems = new Stack<EntityManager>();

        EntityManager getEntityManager() {
            return ems.empty() ? null : ems.peek();
        }

        void beginTransaction() {
            logger.finest("Starting transaction...");

            EntityManager em = emFactory.createEntityManager();
            em.getTransaction().begin();
            ems.push(em);

            logger.finest("Started transaction");
        }

        void commitTransaction() {
            logger.finest("Commiting transaction...");

            EntityManager em = getEntityManager();
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().commit();
            }

            logger.finest("Transaction commited");

            closeEntityManager();
        }

        void rollbackTransaction() {
            logger.finest("Rolling transaction back...");

            EntityManager em = getEntityManager();
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            logger.finest("Transaction rolled back");

            closeEntityManager();
        }

        boolean hasActiveTransaction() {
            if (ems.empty()) { return false; }
            final EntityManager em = getEntityManager();
            return em != null && em.getTransaction().isActive();
        }

        private void closeEntityManager() {
            EntityManager em = ems.empty() ? null : ems.pop();
            if (em == null || !em.isOpen()) {
                logger.warning("TransactionContext already closed");
            } else {
                logger.finest("Closing TransactionContext...");
                try {
                    em.close();

                    Session session = em.unwrap(Session.class);
                    session.disconnect();
                } catch (RuntimeException e) {
                    logger.finest("TransactionalContext closed! " + e.getLocalizedMessage());
                }
            }
        }

        private void close() {
            if (!ems.empty()) {
                logger.warning("TransactionalContext may have transaction leak! Freeing resources for safety...");
                do {
                    closeEntityManager();
                } while (!ems.empty());
            }
        }

    }

    public static final ThreadLocal<InternalContext> transContext = new ThreadLocal<InternalContext>();

    public static final Logger logger = Logger.getLogger(TransactionalContext.class.getName());

    public static EntityManager getEntityManager() {
        final InternalContext ctx = transContext.get();
        return ctx == null ? null : ctx.getEntityManager();
    }

    public synchronized static void config() {
        config(PERSISTENCE_UNIT_NAME);
    }

    public synchronized static void config(String puName) {
        logger.finest("TransactionalContext '" + puName + "' initiated!");
        emFactory = Persistence.createEntityManagerFactory(puName);
    }

    public synchronized static void terminate() {
        if (hasActiveTransaction()) {
            close();
        }
        if (emFactory != null) {
            emFactory.close();
        }
        emFactory = null;
    }

    public static void open() {
        if (emFactory == null) {
            config();
        }

        logger.finest("TransactionalContext started");
        transContext.set(new InternalContext());
    }

    public static void close() {
        if (transContext.get() != null) {
            transContext.get().close();
        }
        transContext.remove();
    }

    public static void beginTransaction() {
        if (!isOpen()) {
            open();
        }
        transContext.get().beginTransaction();
    }

    public static void commitTransaction() {
        if (!isOpen()) {
            logger.severe("Transaction context is not open!");
            throw new RuntimeException("Transaction context is not open!");
        }
        transContext.get().commitTransaction();
    }

    public static void rollbackTransaction() {
        if (!isOpen()) {
            logger.severe("Transaction context is not open!");
            throw new RuntimeException("Transaction context is not open!");
        }
        transContext.get().rollbackTransaction();
    }

    public static boolean hasActiveTransaction() {
        return isOpen() && transContext.get().hasActiveTransaction();
    }

    public static boolean isOpen() {
        return transContext.get() != null;
    }
}