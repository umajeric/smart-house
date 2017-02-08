package si.majeric.smarthouse.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.persistence.SHEntityManagerFactory;

public class GenericDBDAO<T> implements GenericDao<T> {
	protected static Logger logger = LoggerFactory.getLogger(GenericDBDAO.class.getCanonicalName());
	private Class<T> entityClass;

	public GenericDBDAO(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	@Override
	public T findById(String id) {
		EntityManager em = SHEntityManagerFactory.getInstance().createEntityManager();
		T instance = null;
		try {
			em.getTransaction().begin();
			instance = em.find(entityClass, id);
			em.getTransaction().commit();
		} catch (RuntimeException e) {
			throw e;
		} finally {
			em.close();
		}
		return instance;
	}

	@Override
	public List<T> findAll() {
		EntityManager em = SHEntityManagerFactory.getInstance().createEntityManager();
		try {
			CriteriaQuery<T> cq = em.getCriteriaBuilder().createQuery(entityClass);
			Root<T> root = cq.from(entityClass);
			return em.createQuery(cq.select(root)).getResultList();
		} finally {
			em.close();
		}
	}

	@Override
	public int remove(T entity) {
		EntityManager em = SHEntityManagerFactory.getInstance().createEntityManager();
		try {
			em.getTransaction().begin();
			em.remove(entity);
			em.getTransaction().commit();
			return 1;
		} catch (RuntimeException e) {
			logger.error(e.getMessage());
			return 0;
		} finally {
			try {
				em.close();
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public long count() {
		EntityManager em = SHEntityManagerFactory.getInstance().createEntityManager();
		int result = 0;
		try {
			CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
			Root<T> rt = cq.from(entityClass);
			cq.select(em.getCriteriaBuilder().count(rt));
			Query q = em.createQuery(cq);
			result = ((Long) q.getSingleResult()).intValue();
		} catch (RuntimeException e) {
			throw e;
		} finally {
			em.close();
		}
		return result;
	}

	@Override
	public T save(T entity) {
		EntityManager em = SHEntityManagerFactory.getInstance().createEntityManager();
		try {
			em.getTransaction().begin();
			entity = em.merge(entity);
			em.getTransaction().commit();
		} catch (RuntimeException e) {
			throw e;
		} finally {
			em.close();
		}
		return entity;
	}

}
