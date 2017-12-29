package org.bytemechanics.fluentjpa.mocks;

import org.bytemechanics.fluentjpa.PersistenceDao;
import org.bytemechanics.fluentjpa.PersistenceSessionFactory;


/**
 * @author afarre
 */
public class PersistenceDaoMock implements PersistenceDao {

	private final PersistenceSessionFactory persistenceSessionFactory;
	
	public PersistenceDaoMock(final PersistenceSessionFactory _sessionFactory){
		this.persistenceSessionFactory=_sessionFactory;
	}
	
	@Override
	public PersistenceSessionFactory getSessionFactory() {
		return this.persistenceSessionFactory;
	}
}
