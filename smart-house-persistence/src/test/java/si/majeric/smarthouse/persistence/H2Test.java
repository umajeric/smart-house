package si.majeric.smarthouse.persistence;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import junit.framework.TestCase;
import si.majeric.smarthouse.model.Address;
import si.majeric.smarthouse.model.Address.Pin;
import si.majeric.smarthouse.model.Cron;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.model.TriggerConfig;

public class H2Test extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPersistence() {
		try {
			EntityManager em = SHEntityManagerFactory.getInstance().createEntityManager();
			// read the existing entries and write to console
			Query q = em.createQuery("select t from Switch t");
			List<Switch> todoList = q.getResultList();
			for (Switch todo : todoList) {
				System.out.println(todo + ", " + todo.getState());
			}
			System.out.println("Size: " + todoList.size());

			// create new todo
			em.getTransaction().begin();
			Switch swtch = new Switch();
			Address address = new Address();
			address.setPin(Pin.A0);
			address.setProviderAddress(34);
			swtch.setAddress(address);
			swtch.setState(PinState.LOW);
			swtch.setId("KuhinjaRoletaGor");

			TriggerConfig tc = new TriggerConfig();
			tc.setId("Trigger Test");
			tc.setName("TriggerTest");
			tc.setDelay(100l);
			Cron cron = new Cron();
			cron.setExpression("* 1 * * * ?");
			tc.setCron(cron);

			swtch.getTriggers().add(tc);

			em.merge(swtch);
			em.getTransaction().commit();

			todoList = q.getResultList();
			for (Switch todo : todoList) {
				System.out.println(todo + ", " + todo.getState());
			}

			em.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
