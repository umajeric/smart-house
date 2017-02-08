package si.majeric.smarthouse.dao;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.events.SwitchStateChangeEvent;
import si.majeric.smarthouse.model.Address.Pin;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.persistence.SHEntityManagerFactory;

public class SwitchDBDao extends GenericDBDAO<Switch> implements SwitchDao {
	private static final Logger logger = LoggerFactory.getLogger(SwitchDBDao.class);

	public SwitchDBDao() {
		super(Switch.class);
	}

	@Override
	public PinState getStateFor(Switch swch) {
		try {
			if (swch != null && swch.getId() != null) {
				Switch found = findById(swch.getId());
				if (found != null && found.getState() != null) {
					return found.getState();
				}
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return PinState.LOW;
	}
	
	@Override
	public Switch findFor(Integer providerAddress, Pin pin) {
		EntityManager em = SHEntityManagerFactory.getInstance().createEntityManager();
		Switch instance = null;
		try {
			em.getTransaction().begin();
			Query query = em.createQuery("SELECT s FROM Switch s WHERE s.address.providerAddress = :provider AND s.address.pin = :pin") //
					.setParameter("provider", providerAddress) //
					.setParameter("pin", pin);
			
			instance = (Switch) query.getSingleResult();
			em.getTransaction().commit();
		} catch (NoResultException e) {
			em.getTransaction().rollback();
			return null;
		} catch (NonUniqueResultException e) {
			em.getTransaction().rollback();
			throw e;
		} catch (RuntimeException e) {
			em.getTransaction().rollback();
			throw e;
		} finally {
			em.close();
		}
		return instance;
	}

	@Override
	public int save(SwitchStateChangeEvent event) {
		EntityManager em = SHEntityManagerFactory.getInstance().createEntityManager();
		int updated = 0;
		try {
			em.getTransaction().begin();
			Query query = em.createQuery("UPDATE Switch s SET s.state = :state WHERE s.address.providerAddress = :provider AND s.address.pin = :pin") //
					.setParameter("provider", event.getProviderAddress()) //
					.setParameter("pin", Pin.valueOf(event.getPin())) //
					.setParameter("state", event.getNewState());
			updated = query.executeUpdate();
			em.getTransaction().commit();
		} catch (RuntimeException e) {
			em.getTransaction().rollback();
			throw e;
		} finally {
			em.close();
		}
		return updated;
	}
}
