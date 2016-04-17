package transaction;

import java.util.Stack;
import javax.persistence.EntityManager;

import javax.persistence.Persistence; import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionalContext {

    private static class InternalContext {

        private Stack<EntityManager> ems = new Stack<EntityManager>();

        public EntityManager getEntityManager() {
            return ems.peek();
        }

        public void beginTransaction() {
            logger.debug("Starting transaction...");

            EntityManager em = Persistence.createEntityManagerFactory(PersistenceUnitFactory.getUnit())
                    .createEntityManager();
            em.getTransaction().begin();
            ems.push(em);

            logger.debug("Started transaction");
        }

        public void commitTransaction() {
            logger.debug("Commiting transaction...");

            EntityManager em = getEntityManager();
            if (em.getTransaction().isActive()) {
                em.getTransaction().commit();
            }

            logger.debug("Transaction commited");

            closeEntityManager();
        }

        public void rollbackTransaction() {
            logger.debug("Rolling transaction back...");

            EntityManager em = getEntityManager();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            logger.debug("Transaction rolled back");

            closeEntityManager();
        }

        private void closeEntityManager() {
            EntityManager em = ems.pop();
            if (em == null || !em.isOpen()) {
                logger.warn("TransactionContext already closed");
            } else {
                logger.debug("Closing TransactionContext...");
                try {
                    em.close();
                    Session session = em.unwrap(Session.class);
                    session.disconnect();
                } catch (RuntimeException e) {
                    logger.debug("TransactionalContext closed!", e);
                }
            }
        }

        private void close() {
            if (!ems.isEmpty()) {
                logger.warn("TransactionalContext may have transaction leak! Freeing resources for safety...");
                do {
                    closeEntityManager();
                } while (!ems.isEmpty());
            }
        }

    }

    public static final ThreadLocal<InternalContext> transContext = new ThreadLocal<InternalContext>();

    public static final Logger logger = LoggerFactory.getLogger(TransactionalContext.class);

    public static EntityManager getEntityManager() {
        return transContext.get().getEntityManager();
    }

    public static void open() {
        logger.debug("TransactionalContext started");
        transContext.set(new InternalContext());
    }

    public static void close() {
        transContext.get().close();
        transContext.remove();
    }

    public static void beginTransaction() {
        if (!isOpen()) {
            open();
        }
        transContext.get().beginTransaction();
    }

    public static void commitTransaction() {
        transContext.get().commitTransaction();
    }

    public static void rollbackTransaction() {
        transContext.get().rollbackTransaction();
    }

    public static boolean isOpen() {
        return transContext.get() != null;
    }

}
