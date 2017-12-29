package org.bytemechanics.fluentjpa.mocks;

import org.bytemechanics.fluentjpa.PersistenceSessionFactory;
import org.bytemechanics.fluentjpa.PersistenceSessionManager;


/**
 * @author afarre
 */
public class PersistenceSessionFactoryMock implements PersistenceSessionFactory{

	public final PersistenceSessionManager sessionManager;
	
	
	public PersistenceSessionFactoryMock(final PersistenceSessionManager _sessionManager){
		this.sessionManager=_sessionManager;
	}
	
	@Override
	public PersistenceSessionManager getSessionManager() {
		return sessionManager;
	}
	
}
