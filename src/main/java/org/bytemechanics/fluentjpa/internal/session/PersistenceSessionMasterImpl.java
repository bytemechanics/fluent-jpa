package org.bytemechanics.fluentjpa.internal.session;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.bytemechanics.fluentjpa.PersistenceSessionManager;
import org.bytemechanics.fluentjpa.PersistenceTransaction;
import org.bytemechanics.fluentjpa.internal.transactions.PersistenceTransactionManager;
import org.bytemechanics.fluentjpa.internal.utils.SimpleFormat;

/**
 *
 * @author afarre
 */
public class PersistenceSessionMasterImpl extends PersistenceSessionDelegateImpl{
	
	private final static String KIND="MASTER";
	private final static Logger logger=Logger.getLogger(PersistenceSessionMasterImpl.class.getSimpleName());

	protected final PersistenceSessionManager sessionManager;
	protected final PersistenceTransactionManager transactionManager; 

	public PersistenceSessionMasterImpl(final EntityManager _entityManager,final PersistenceSessionManager _sessionManager) {
		super(_entityManager);
		this.sessionManager=_sessionManager;
		this.transactionManager=new PersistenceTransactionManager(() -> this.session.getTransaction());
	}

	
	@Override
	protected String getKind(){
		return KIND;
	}

	@Override
	public EntityTransaction getTransaction() {
		return currentTransaction();
	}
	@Override
	public PersistenceTransaction currentTransaction() {
		return this.transactionManager.getTransaction();
	}
	
	@Override
	public void close() {
		logger.log(Level.FINEST, SimpleFormat.supplier("persistence-session[{}]::close::{}",KIND,this));
		try{
			this.session.close();
		}catch(Throwable e){
			logger.log(Level.WARNING,e,SimpleFormat.supplier("persistence-session[{}]::close::{}::close::failed",KIND,this));
		}
		try{
			this.sessionManager.removeSession();
		}catch(Throwable e){
			logger.log(Level.WARNING,e,SimpleFormat.supplier("persistence-session[{}]::close::{}::remove::failed",KIND,this));
		}
	}
	

	@Override
	public String toString() {
		return SimpleFormat.format("PersistenceSessionMasterImpl[session={}]",this.session);
	}	
}
