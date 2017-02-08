package si.majeric.smarthouse.dao;

import java.util.List;

public interface GenericDao<T> {
	T findById(String id);

	T save(T configuration);

	int remove(T entity);

	long count();

	List<T> findAll();
}
