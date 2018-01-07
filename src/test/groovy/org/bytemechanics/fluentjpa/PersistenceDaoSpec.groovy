package org.bytemechanics.fluentjpa

import org.bytemechanics.fluentjpa.mocks.*
import org.bytemechanics.fluentjpa.*
import org.bytemechanics.fluentjpa.exceptions.*
import org.bytemechanics.fluentjpa.exceptions.DuplicateEntryException
import org.bytemechanics.fluentjpa.exceptions.UnknownEntryException
import org.bytemechanics.fluentjpa.internal.utils.SimpleFormat
import spock.lang.*
import spock.mock.*
import spock.util.*
import spock.config.*
import java.util.logging.*
import java.io.*
import javax.persistence.*

/**
 * @author afarre
 */
class PersistenceDaoSpec extends Specification {

	def setupSpec(){
		println(">>>>> PersistenceDaoSpec >>>> setupSpec")
		final InputStream inputStream = PersistenceDao.class.getResourceAsStream("/logging.properties");
		try{
			LogManager.getLogManager().readConfiguration(inputStream);
		}catch (final IOException e){
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}finally{
			if(inputStream!=null)
				inputStream.close();
		}
	}

	def PersistenceSessionManager sessionManager
	def PersistenceSessionFactory sessionFactory
	def EntitySessionMock currentRealSession
	def EntityTransactionMock currentTransaction
	def EntityManagerFactory entityManagerFactory
	def PersistenceUnitUtil persistenceUnitUtil
	
	def setup(){
		println(">>>>> PersistenceDaoSpec >>>> setup")
		this.entityManagerFactory=Mock(EntityManagerFactory)
		this.persistenceUnitUtil=Mock(PersistenceUnitUtil)
		EntitySessionMock.cleanup()
		this.currentTransaction=new EntityTransactionMock()
		this.sessionManager=new PersistenceSessionManager(this.entityManagerFactory,
			{properties -> 
				currentRealSession=new EntitySessionMock(currentTransaction) 
				return currentRealSession
			})
		this.sessionFactory=new PersistenceSessionFactoryMock(sessionManager);
	}
	def cleanup(){
		println(">>>>> PersistenceDaoSpec >>>> cleanup")
		this.sessionManager=null
		this.sessionFactory=null
		this.currentRealSession=null
		this.currentTransaction=null
		this.entityManagerFactory=null
		this.persistenceUnitUtil=null
	}
/*
	def "Transactional Query must start and commit transaction"(){
		println(">>>>> PersistenceDaoSpec >>>>  Transactional Query must start and commit transaction")

		setup:
			def PersistenceDao persistenceDao=new PersistenceDaoMock(this.sessionFactory);
			def Optional<Integer> result;
			
		when:
			result=persistenceDao.transactional(
					Integer.class,
					{persistenceSession -> 
						persistenceSession.persist(new EntityMock("A","B"))
						return 1
					})
			
		then:
			result.get()==1
			this.currentRealSession.getRepo().get("A").getValue().equals("B")
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==true
			this.currentTransaction.hasBeenRollbacked()==false
			this.currentTransaction.isActive()==false
	}
	def "Transactional Query error must start and rollback transaction returning the exception"(){
		println(">>>>> PersistenceDaoSpec >>>>  Transactional Query error must start and rollback transaction returning the exception")

		setup:
			def PersistenceDao persistenceDao=new PersistenceDaoMock(this.sessionFactory);
			def Optional<Integer> result;
			
		when:
			result=persistenceDao.transactional(
					Integer.class,
					{persistenceSession -> 
						throw new RuntimeException("Failure");
						return 1
					})
			
		then:
			def exception=thrown(RuntimeException)
			exception.getMessage().equals("Failure")
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==false
			this.currentTransaction.hasBeenRollbacked()==true
			this.currentTransaction.isActive()==false
	}
*/
	def "save entity without id should launch an exception"(){
		println(">>>>> PersistenceDaoSpec >>>>  save entity without id should launch an exception")
		
		setup:
			def PersistenceDao persistenceDao=new PersistenceDaoMock(this.sessionFactory);
			def EntityMock entity=new EntityMock("id1","value1")
			
		when:
			persistenceDao.save(EntityMock.class,entity)
			
		then:
			1 * this.entityManagerFactory.getPersistenceUnitUtil() >> this.persistenceUnitUtil
			1 * this.persistenceUnitUtil.getIdentifier(entity) >> null
			thrown(EntityWithoutIdOrNotAnEntity)
	}
	def "save entity should start and end transaction and persist the entity"(){
		println(">>>>> PersistenceDaoSpec >>>>  save entity should start and end transaction and persist the entity")
		
		setup:
			def PersistenceDao persistenceDao=new PersistenceDaoMock(this.sessionFactory);
			def EntityMock entity=new EntityMock("id1","value1")
			
		when:
			persistenceDao.save(EntityMock.class,entity)
			
		then:
			1 * this.entityManagerFactory.getPersistenceUnitUtil() >> this.persistenceUnitUtil
			1 * this.persistenceUnitUtil.getIdentifier(entity) >> "id1"
			this.currentRealSession.getRepo().get("id1").getValue().equals("value1")
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==true
			this.currentTransaction.hasBeenRollbacked()==false
			this.currentTransaction.isActive()==false
	}

 	def "save entity with duplicated id should raise an exception and rollback the transaction"(){
		println(">>>>> PersistenceDaoSpec >>>>  save entity with duplicated id should raise an exception and rollback the transaction")

		setup:
			def PersistenceDao persistenceDao=new PersistenceDaoMock(this.sessionFactory);
			def EntityMock entity=new EntityMock("id1","value1")
			def PersistenceSession session=persistenceDao.getSessionFactory().getSession();
			this.currentRealSession.persist(entity)
			session.close();
			
		when:
			persistenceDao.save(EntityMock.class,entity)
			
		then:
			1 * this.entityManagerFactory.getPersistenceUnitUtil() >> this.persistenceUnitUtil
			1 * this.persistenceUnitUtil.getIdentifier(entity) >> "id1"
			def exception=thrown(DuplicateEntryException)
			exception.getMessage().equals(SimpleFormat.format(DuplicateEntryException.MESSAGE,"EntityMock","id1"))
			this.currentRealSession.getRepo().get("id1").getValue().equals("value1")
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==false
			this.currentTransaction.hasBeenRollbacked()==true
			this.currentTransaction.isActive()==false
	}

	def "contains entity should start and end transaction and persist the entity and return true if exist"(){
		println(">>>>> PersistenceDaoSpec >>>>  contains entity should start and end transaction and persist the entity and return true if exist")

		setup:
			def PersistenceDao persistenceDao=new PersistenceDaoMock(this.sessionFactory);
			def EntityMock entity=new EntityMock("id1","value1")
			def PersistenceSession session=persistenceDao.getSessionFactory().getSession();
			this.currentRealSession.persist(entity)
			session.close();
			def result
			
		when:
			result=persistenceDao.contains(EntityMock.class,"id1")
			
		then:
			result==true
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==true
			this.currentTransaction.hasBeenRollbacked()==false
			this.currentTransaction.isActive()==false
	}
	def "contains entity should start and end transaction and persist the entity and return false if not exist"(){
		println(">>>>> PersistenceDaoSpec >>>>  contains entity should start and end transaction and persist the entity and return false if not exist")

		setup:
			def PersistenceDao persistenceDao=new PersistenceDaoMock(this.sessionFactory);
			def EntityMock entity=new EntityMock("id1","value1")
			def PersistenceSession session=persistenceDao.getSessionFactory().getSession();
			this.currentRealSession.persist(entity)
			session.close();
			def result
			
		when:
			result=persistenceDao.contains(EntityMock.class,"id2")
			
		then:
			result==false
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==true
			this.currentTransaction.hasBeenRollbacked()==false
			this.currentTransaction.isActive()==false
	}

	def "get entity should start and end transaction and persist the entity"(){
		println(">>>>> PersistenceDaoSpec >>>>  get entity should start and end transaction and persist the entity")

		setup:
			def PersistenceDao persistenceDao=new PersistenceDaoMock(this.sessionFactory);
			def EntityMock entity=new EntityMock("id1","value1")
			def PersistenceSession session=persistenceDao.getSessionFactory().getSession();
			this.currentRealSession.persist(entity)
			session.close();
			def result
			
		when:
			result=persistenceDao.get(EntityMock.class,entity.getId())
			
		then:
			result!=null
			result.getId().equals("id1")
			result.getValue().equals("value1")
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==true
			this.currentTransaction.hasBeenRollbacked()==false
			this.currentTransaction.isActive()==false
	}
	def "get entity of unknown id should raise an exception and rollback the transaction"(){
		println(">>>>> PersistenceDaoSpec >>>>  get entity of unknown id should raise an exception and rollback the transaction")

		setup:
			def PersistenceDao persistenceDao=new PersistenceDaoMock(this.sessionFactory);
			def EntityMock entity=new EntityMock("id1","value1")
			def PersistenceSession session=persistenceDao.getSessionFactory().getSession();
			this.currentRealSession.persist(entity)
			session.close();
			def result
			
		when:
			persistenceDao.get(EntityMock.class,"id2")
			
		then:
			def exception=thrown(UnknownEntryException)
			exception.getMessage().equals(SimpleFormat.format(UnknownEntryException.MESSAGE,"get","EntityMock","id2"))
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==false
			this.currentTransaction.hasBeenRollbacked()==true
			this.currentTransaction.isActive()==false
	}

	def "update entity should start and end transaction and merge the entity"(){
		println(">>>>> PersistenceDaoSpec >>>>  update entity should start and end transaction and merge the entity")

		setup:
			def PersistenceDao persistenceDao=new PersistenceDaoMock(this.sessionFactory);
			def EntityMock entity=new EntityMock("id1","value1")
			def PersistenceSession session=persistenceDao.getSessionFactory().getSession();
			this.currentRealSession.persist(entity)
			session.close();
			def result
			
		when:
			entity.setValue("value2")
			result=persistenceDao.update(EntityMock.class,entity)
			
		then:
			1 * this.entityManagerFactory.getPersistenceUnitUtil() >> this.persistenceUnitUtil
			1 * this.persistenceUnitUtil.getIdentifier(entity) >> "id1"
			result!=null
			result.getId().equals("id1")
			result.getValue().equals("value2")
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==true
			this.currentTransaction.hasBeenRollbacked()==false
			this.currentTransaction.isActive()==false
	}
	def "update entity of unknown id should raise an exception and rollback the transaction"(){
		println(">>>>> PersistenceDaoSpec >>>>  update entity of unknown id should raise an exception and rollback the transaction")

		setup:
			def PersistenceDao persistenceDao=new PersistenceDaoMock(this.sessionFactory);
			def EntityMock entity=new EntityMock("id1","value1")
			def PersistenceSession session=persistenceDao.getSessionFactory().getSession();
			this.currentRealSession.persist(entity)
			session.close();
			
		when:
			def EntityMock entity2=new EntityMock("id2","value2")
			persistenceDao.update(EntityMock.class,entity2)
			
		then:
			1 * this.entityManagerFactory.getPersistenceUnitUtil() >> this.persistenceUnitUtil
			1 * this.persistenceUnitUtil.getIdentifier(entity) >> "id1"
			def exception=thrown(UnknownEntryException)
			exception.getMessage().equals(SimpleFormat.format(UnknownEntryException.MESSAGE,"update","EntityMock","id2"))
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==false
			this.currentTransaction.hasBeenRollbacked()==true
			this.currentTransaction.isActive()==false
	}

	
	def "delete entity should start and end transaction and remove the entity"(){
		println(">>>>> PersistenceDaoSpec >>>>  delete entity should start and end transaction and remove the entity")

		setup:
			def PersistenceDao persistenceDao=new PersistenceDaoMock(this.sessionFactory);
			def EntityMock entity=new EntityMock("id1","value1")
			def PersistenceSession session=persistenceDao.getSessionFactory().getSession();
			this.currentRealSession.persist(entity)
			session.close();
			
		when:
			persistenceDao.delete(EntityMock.class,entity)
			
		then:
			1 * this.entityManagerFactory.getPersistenceUnitUtil() >> this.persistenceUnitUtil
			1 * this.persistenceUnitUtil.getIdentifier(entity) >> "id1"
			this.currentRealSession.getRepo().get("id1")==null
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==true
			this.currentTransaction.hasBeenRollbacked()==false
			this.currentTransaction.isActive()==false
	}
	def "delete entity of unknown id should raise an exception and rollback the transaction"(){
		println(">>>>> PersistenceDaoSpec >>>>  delete entity of unknown id should raise an exception and rollback the transaction")

		setup:
			def PersistenceDao persistenceDao=new PersistenceDaoMock(this.sessionFactory);
			def EntityMock entity=new EntityMock("id1","value1")
			def PersistenceSession session=persistenceDao.getSessionFactory().getSession();
			this.currentRealSession.persist(entity)
			session.close();
			
		when:
			def EntityMock entity2=new EntityMock("id2","value2")
			persistenceDao.delete(EntityMock.class,entity2)
			
		then:
			1 * this.entityManagerFactory.getPersistenceUnitUtil() >> this.persistenceUnitUtil
			2 * this.persistenceUnitUtil.getIdentifier(entity) >> "id1"
			def exception=thrown(UnknownEntryException)
			exception.getMessage().equals(SimpleFormat.format(UnknownEntryException.MESSAGE,"delete","EntityMock","id2"))
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==false
			this.currentTransaction.hasBeenRollbacked()==true
			this.currentTransaction.isActive()==false
	}
}

