package org.bytemechanics.fluentjpa;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytemechanics.fluentjpa.exceptions.DuplicateEntryException;
import org.bytemechanics.fluentjpa.exceptions.EntityWithoutIdOrNotAnEntity;
import org.bytemechanics.fluentjpa.exceptions.MandatoryParameterException;
import org.bytemechanics.fluentjpa.exceptions.PersistenceOperationException;
import org.bytemechanics.fluentjpa.exceptions.UnknownEntryException;
import org.bytemechanics.fluentjpa.internal.utils.SimpleFormat;

/**
 * @author afarre
 */
public interface PersistenceDao {

	public PersistenceSessionFactory getSessionFactory();
	
	public default void transactional(final Consumer<PersistenceSession> _consumer){
		transactional(Object.class,persistenceSession -> {
											_consumer.accept(persistenceSession);
											return null;
									});
	}
	public default <T> Optional<T> transactional(final Class<T> _class,final Function<PersistenceSession,T> _queryExecutor){
		
		final Logger logger=Logger.getLogger(this.getClass().getSimpleName());
		Optional<T> reply;
		
		logger.log(Level.FINER,SimpleFormat.supplier("persistence-dao::transation::for::{}::of::{}::begin",_queryExecutor,_class));
		try(PersistenceSession session=getSessionFactory().getSession()){
			logger.log(Level.FINEST,SimpleFormat.supplier("persistence-dao::transation::for::{}::of::{}::session::{}::beguin",_queryExecutor,_class,session));
			try(PersistenceTransaction transaction=session.currentTransaction()){
				logger.log(Level.FINEST,SimpleFormat.supplier("persistence-dao::transation::for::{}::of::{}::session::{}::transaction::{}::beguin",_queryExecutor,_class,session,transaction));
				transaction.begin();
				reply=Optional.ofNullable(_queryExecutor.apply(session));
				transaction.commit();
				logger.log(Level.FINEST,SimpleFormat.supplier("persistence-dao::transation::for::{}::of::{}::session::{}::transaction::{}::end::{}",_queryExecutor,_class,session,transaction,reply));
			}
			logger.log(Level.FINER,SimpleFormat.supplier("persistence-dao::transation::for::{}::of::{}::session::{}::end::{}",_queryExecutor,_class,session,reply));
		}
		logger.log(Level.FINE,SimpleFormat.supplier("persistence-dao::transation::for::{}::of::{}::end::{}",_queryExecutor,_class,reply));
		
		return reply;
	}	


	public default Object getId(final String _action,final String _entityType,final Object _entity){
		
		final Logger logger=Logger.getLogger(this.getClass().getSimpleName());
		Object reply;
		
		logger.log(Level.FINER,SimpleFormat.supplier("persistence-dao::get-id::from::{}::begin",_entity));
		reply=Optional.ofNullable(getSessionFactory())
						.map(sessionFactory -> {logger.log(Level.FINER,SimpleFormat.supplier("persistence-dao::get-id::from::{}::session-factory::{}",_entity,sessionFactory)); return sessionFactory;})
						.map(sessionFactory -> sessionFactory.getPersistenceUnitUtil())
						.map(persistenceUtil -> {logger.log(Level.FINER,SimpleFormat.supplier("persistence-dao::get-id::from::{}::persistence-util::{}",_entity,persistenceUtil)); return persistenceUtil;})
						.filter(persistenceUtil -> persistenceUtil!=null)
						.map(persistenceUtil -> persistenceUtil.getIdentifier(_entity))
						.orElseThrow(() -> new EntityWithoutIdOrNotAnEntity(_action,_entityType,_entity));
		logger.log(Level.FINE,SimpleFormat.supplier("persistence-dao::get-id::from::{}::end::{}",_entity,reply));
		
		return reply;
	}
	
	@SuppressWarnings("UseSpecificCatch")
	public default <T> T save(final Class<T> _entityClass,final T _entity) {

		final Logger logger=Logger.getLogger(this.getClass().getSimpleName());

		try{
			logger.log(Level.FINER,SimpleFormat.supplier("persistence-dao::save::{}::of::{}::begin",_entity,_entityClass));
			final String entityType=Optional.ofNullable(_entityClass)
										.map(entity -> entity.getSimpleName())
										.orElseThrow(() -> new MandatoryParameterException("save","Unknown","_entityClass"));
			final Object entityId=Optional.ofNullable(_entity)
										.map(entity -> getId("save",entityType,entity))
										.orElseThrow(() -> new MandatoryParameterException("save",entityType,"_entity"));
			transactional(sessionManager -> {
				if(sessionManager.find(_entityClass, entityId)!=null){
					throw new DuplicateEntryException(entityType,entityId);
				}
				sessionManager.persist(_entity);
			});
			logger.log(Level.FINE,SimpleFormat.supplier("persistence-dao::save::{}::of::{}::end",_entity,_entityClass));
		}catch(DuplicateEntryException|MandatoryParameterException|EntityWithoutIdOrNotAnEntity e){
			logger.log(Level.FINE,SimpleFormat.supplier("persistence-dao::save::{}::of::{}::failed::{}",_entity,_entityClass,e.getMessage()));
			throw e;
		}catch(Exception e){
			logger.log(Level.FINE,e,SimpleFormat.supplier("persistence-dao::save::{}::of::{}::failed::{}",_entity,_entityClass,e.getMessage()));
			throw new PersistenceOperationException("save", _entityClass, _entity,e);
		}
		
		return _entity;
	}

	@SuppressWarnings("UseSpecificCatch")
	public default <T> boolean contains(final Class<T> _entityClass,final Object _entityId) {

		final Logger logger=Logger.getLogger(this.getClass().getSimpleName());
		Boolean reply;

		try{
			logger.log(Level.FINER,SimpleFormat.supplier("persistence-dao::contains::{}::of::{}::begin",_entityId,_entityClass));
			final String entityType=Optional.ofNullable(_entityClass)
										.map(entity -> entity.getSimpleName())
										.orElseThrow(() -> new MandatoryParameterException("contains","Unknown","_entityClass"));
			final Object entityId=Optional.ofNullable(_entityId)
										.orElseThrow(() -> new MandatoryParameterException("contains",entityType,"_entityId"));
			reply=transactional(Boolean.class,sessionManager -> 
					sessionManager.find(_entityClass, entityId)!=null)
					.orElse(false);
			logger.log(Level.FINE,SimpleFormat.supplier("persistence-dao::contains::{}::of::{}::end::{}",_entityId,_entityClass,reply));
		}catch(MandatoryParameterException e){
			logger.log(Level.FINE,SimpleFormat.supplier("persistence-dao::contains::{}::of::{}::failed::{}",_entityId,_entityClass,e.getMessage()));
			throw e;
		}catch(Exception e){
			logger.log(Level.FINE,e,SimpleFormat.supplier("persistence-dao::contains::{}::of::{}::failed::{}",_entityId,_entityClass,e.getMessage()));
			throw new PersistenceOperationException("contains", _entityClass, _entityId,e);
		}
		
		return reply;
	}

	@SuppressWarnings("UseSpecificCatch")
	public default <T> T get(final Class<T> _entityClass,final Object _entityId) {

		final Logger logger=Logger.getLogger(this.getClass().getSimpleName());
		T reply;

		try{
			logger.log(Level.FINER,SimpleFormat.supplier("persistence-dao::get::{}::of::{}::begin",_entityId,_entityClass));
			final String entityType=Optional.ofNullable(_entityClass)
										.map(entity -> entity.getSimpleName())
										.orElseThrow(() -> new MandatoryParameterException("get","Unknown","_entityClass"));
			final Object entityId=Optional.ofNullable(_entityId)
										.orElseThrow(() -> new MandatoryParameterException("get",entityType,"_entityId"));
			reply=transactional(_entityClass,sessionManager -> 
					Optional.ofNullable(sessionManager.find(_entityClass, entityId))
						.orElseThrow(() -> new UnknownEntryException("get",entityType,entityId)))
				.orElseThrow(() -> new PersistenceOperationException("get",entityType,entityId));
			logger.log(Level.FINE,SimpleFormat.supplier("persistence-dao::get::{}::of::{}::end::{}",_entityId,_entityClass,reply));
		}catch(UnknownEntryException|MandatoryParameterException|PersistenceOperationException e){
			logger.log(Level.FINE,SimpleFormat.supplier("persistence-dao::get::{}::of::{}::failed::{}",_entityId,_entityClass,e.getMessage()));
			throw e;
		}catch(Exception e){
			logger.log(Level.FINE,e,SimpleFormat.supplier("persistence-dao::get::{}::of::{}::failed::{}",_entityId,_entityClass,e.getMessage()));
			throw new PersistenceOperationException("retrieve",_entityClass,_entityId,e);
		}
		
		return reply;
	}

	@SuppressWarnings("UseSpecificCatch")
	public default <T> T update(final Class<T> _entityClass,final T _entity) {

		final Logger logger=Logger.getLogger(this.getClass().getSimpleName());
		T reply;

		try{
			logger.log(Level.FINER,SimpleFormat.supplier("persistence-dao::update::{}::of::{}::begin",_entity,_entityClass));
			final String entityType=Optional.ofNullable(_entityClass)
										.map(entity -> entity.getSimpleName())
										.orElseThrow(() -> new MandatoryParameterException("update","Unknown","_entityClass"));
			final Object entityId=Optional.ofNullable(_entity)
										.map(entity -> getId("update",entityType,_entity))
										.orElseThrow(() -> new MandatoryParameterException("update",entityType,"_entity"));
			reply=transactional(_entityClass,sessionManager -> {
						if(sessionManager.find(_entityClass, entityId)==null){
							throw new UnknownEntryException("update",entityType,entityId);
						}
						return sessionManager.merge(_entity);
					})
					.orElseThrow(() -> new PersistenceOperationException("update",entityType,entityId));
			logger.log(Level.FINE,SimpleFormat.supplier("persistence-dao::update::{}::of::{}::end::{}",_entity,_entityClass,reply));
		}catch(MandatoryParameterException|UnknownEntryException|PersistenceOperationException|EntityWithoutIdOrNotAnEntity e){
			logger.log(Level.FINE,SimpleFormat.supplier("persistence-dao::update::{}::of::{}::failed::{}",_entity,_entityClass,e.getMessage()));
			throw e;
		}catch(Exception e){
			logger.log(Level.FINE,e,SimpleFormat.supplier("persistence-dao::update::{}::of::{}::failed::{}",_entity,_entityClass,e.getMessage()));
			throw new PersistenceOperationException("update",_entityClass,_entity);
		}
		
		return reply;
	}

	@SuppressWarnings("UseSpecificCatch")
	public default <T> void delete(final Class<T> _entityClass,final Object _entityId) {

		final Logger logger=Logger.getLogger(this.getClass().getSimpleName());

		try{
			logger.log(Level.FINER,SimpleFormat.supplier("persistence-dao::delete::{}::of::{}::begin",_entityId,_entityClass));
			final String entityType=Optional.ofNullable(_entityClass)
										.map(entity -> entity.getSimpleName())
										.orElseThrow(() -> new MandatoryParameterException("delete","Unknown","_entityClass"));
			final Object entityId=Optional.ofNullable(_entityId)
										.orElseThrow(() -> new MandatoryParameterException("delete",entityType,"_entityId"));
			transactional(sessionManager -> {
				final T entity=Optional.ofNullable(sessionManager.find(_entityClass, entityId))
						.orElseThrow(() -> new UnknownEntryException("delete",entityType,entityId));
				sessionManager.remove(entity);
			});
			logger.log(Level.FINE,SimpleFormat.supplier("persistence-dao::delete::{}::of::{}::end",_entityId,_entityClass));
		}catch(MandatoryParameterException|UnknownEntryException e){
			logger.log(Level.FINE,SimpleFormat.supplier("persistence-dao::delete::{}::of::{}::failed::{}",_entityId,_entityClass,e.getMessage()));
			throw e;
		}catch(Exception e){
			logger.log(Level.FINE,e,SimpleFormat.supplier("persistence-dao::delete::{}::of::{}::failed::{}",_entityId,_entityClass,e.getMessage()));
			throw new PersistenceOperationException("delete",_entityClass,_entityId,e);
		}
	}
}
