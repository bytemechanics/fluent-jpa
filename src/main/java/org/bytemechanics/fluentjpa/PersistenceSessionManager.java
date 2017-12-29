package org.bytemechanics.fluentjpa;

import java.io.Closeable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnitUtil;
import org.bytemechanics.fluentjpa.exceptions.PersistenceSessionCreationException;
import org.bytemechanics.fluentjpa.exceptions.PersistenceSessionSupplierException;
import org.bytemechanics.fluentjpa.internal.session.PersistenceSessionMasterImpl;
import org.bytemechanics.fluentjpa.internal.session.PersistenceSessionSlaveImpl;
import org.bytemechanics.fluentjpa.internal.utils.SimpleFormat;

/**
 * @author afarre
 */
public class PersistenceSessionManager implements Closeable{

	private final static Logger logger=Logger.getLogger(PersistenceSessionManager.class.getSimpleName());
	
	private final ThreadLocal<PersistenceSession> underlayingSessionThreadLocal;
	private final Function<Map<String,String>,EntityManager> sessionSupplier;
	private final EntityManagerFactory entityManagerFactory;
	
	public PersistenceSessionManager(final String _persistenceUnit){
		this(_persistenceUnit,Collections.emptyMap());
	}
	public PersistenceSessionManager(final String _persistenceUnit,final Map<String,String> _properties){
		this.underlayingSessionThreadLocal=new ThreadLocal<>();
		this.entityManagerFactory=createFactory(_persistenceUnit,_properties);
		this.sessionSupplier=(properties) -> this.entityManagerFactory.createEntityManager(properties);
	}
	public PersistenceSessionManager(final EntityManagerFactory _sessionFactory){
		this((properties) -> (PersistenceSession)_sessionFactory.createEntityManager(properties),_sessionFactory);
	}
	public PersistenceSessionManager(final Function<Map<String,String>,EntityManager> _sessionSupplier){
		this(_sessionSupplier,null);
	}
	public PersistenceSessionManager(final Function<Map<String,String>,EntityManager> _sessionSupplier,final EntityManagerFactory _sessionFactory){
		this.underlayingSessionThreadLocal=new ThreadLocal<>();
		this.sessionSupplier=_sessionSupplier;
		this.entityManagerFactory=_sessionFactory;
	}

	private EntityManagerFactory createFactory(final String _persistenceUnit,final Map<String,String> _properties){
		
		EntityManagerFactory reply=null;
		
		try{
			reply=Persistence.createEntityManagerFactory(_persistenceUnit,_properties);
		}catch(Throwable e){
			logger.log(Level.WARNING, e, SimpleFormat.supplier("persistence-session-manager::create-factory::{}::with-properties::{}::failed",_persistenceUnit,_properties));
			throw new PersistenceSessionCreationException(_persistenceUnit,_properties,e);
		}
		
		return reply;
	}
	private Optional<PersistenceSession> get(){
		return Optional.ofNullable(this.underlayingSessionThreadLocal.get());
	}
	private PersistenceSession createLocalSession(){
		
		final PersistenceSession reply=createSession();

		this.underlayingSessionThreadLocal.set(reply);
		logger.log(Level.FINEST, SimpleFormat.supplier("persistence-session-manager::register::local-session::{}",this.underlayingSessionThreadLocal.get()));

		return reply;
	}
	
	
	public PersistenceSession createSession() {
		return createSession(Collections.emptyMap());
	}
	public PersistenceSession createSession(final Map<String,String> _properties) {
		return Optional.ofNullable(this.sessionSupplier.apply(_properties))
						.map(EntityManager -> new PersistenceSessionMasterImpl(EntityManager,this))
						.orElseThrow(() -> new PersistenceSessionSupplierException());
	}
	public PersistenceSession currentSession(){
		return this.underlayingSessionThreadLocal.get();
	}
	public PersistenceSession getSession() {
		return get()
			.map(persistenceSession -> new PersistenceSessionSlaveImpl(persistenceSession))
			.map(persistenceSession -> (PersistenceSession)persistenceSession)
			.orElseGet(this::createLocalSession);
	}
	public PersistenceUnitUtil getPersistenceUnitUtil(){
		return this.entityManagerFactory.getPersistenceUnitUtil();
	}	

	public void removeSession() {
		logger.log(Level.FINEST, SimpleFormat.supplier("persistence-session-manager::unregister::local-session::{}",this.underlayingSessionThreadLocal.get()));
		this.underlayingSessionThreadLocal.remove();
	}
	public boolean hasSession(){
		return this.underlayingSessionThreadLocal.get()!=null;
	}
	public void init(){
		if(this.entityManagerFactory!=null){
			this.entityManagerFactory.isOpen();
		}
	}
	@Override
	public void close(){
		if((this.entityManagerFactory!=null)&&(this.entityManagerFactory.isOpen())){
			this.entityManagerFactory.close();
		}
	}
}
