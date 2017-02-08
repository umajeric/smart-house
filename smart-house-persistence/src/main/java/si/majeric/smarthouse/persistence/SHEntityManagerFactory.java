package si.majeric.smarthouse.persistence;

import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class SHEntityManagerFactory {
	public static final String PERSISTENCE_UNIT_NAME = "smartHousePU";
	private static final String ENTITY_MANAGER_FACTORY_JNDI = "java:/smartHouseEntityManagerFactory";

	private static EntityManagerFactory emf;

	private SHEntityManagerFactory() {
	}

	/**
	 * Return the EntityManagerFactory, or initialize it if it is null.
	 * 
	 * @return EntityManagerFactory
	 */
	public static EntityManagerFactory getInstance() {
		if (emf == null) {
			init();
		}
		return emf;
	}

	/**
	 * Initialize the entityManagerFactory by grabbing it out of the JNDI InitialContext. It is very important that the example uses the same
	 * EntityManagerFactory object that is initialized by the ESB Hibernate Listener because hibernate events can only be intercepted on the
	 * EntityManagerFactory that the interceptor is set on. By grabbing the EntityManagerFactory out of JNDI, we guarantee that we do that, even in the case of
	 * a .ear redeploy.
	 */
	private static synchronized void init() {
		try {
			InitialContext ic = new InitialContext();
			emf = (EntityManagerFactory) ic.lookup(ENTITY_MANAGER_FACTORY_JNDI);
		} catch (Exception e) {
		}

		if (emf == null) {
			emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		}
	}

	/**
	 * Close the EntityManagerFactory and set the class's entityManagerFactory to null.
	 */
	public static void close() {
		if (emf != null) {
			emf.close();
		}
		emf = null;
	}

}
