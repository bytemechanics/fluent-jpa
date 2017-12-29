package org.bytemechanics.fluentjpa

import org.bytemechanics.fluentjpa.tests.LoggingSpecification
import org.bytemechanics.fluentjpa.mocks.*
import org.bytemechanics.fluentjpa.*
import org.bytemechanics.fluentjpa.exceptions.*
import org.bytemechanics.fluentjpa.exceptions.DuplicateEntryException
import org.bytemechanics.fluentjpa.exceptions.UnknownEntryException
import spock.lang.*

/**
 * @author afarre
 */
class PersistenceDaoSpec extends LoggingSpecification {

	def PersistenceSessionManager sessionManager
	def PersistenceSessionFactory sessionFactory
	def EntitySessionMock currentRealSession
	def EntityTransactionMock currentTransaction
	
	def setup(){
		EntitySessionMock.cleanup()
		this.currentTransaction=new EntityTransactionMock()
		this.sessionManager=new PersistenceSessionManager(
			{properties -> 
				currentRealSession=new EntitySessionMock(currentTransaction) 
				return currentRealSession
			})
		this.sessionFactory=new PersistenceSessionFactoryMock(sessionManager);
	}
	def cleanup(){
		this.sessionManager=null
		this.sessionFactory=null;
	}

	def "Transactional Query must start and commit transaction"(){
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
	def "save entity should start and end transaction and persist the entity"(){
		setup:
			def PersistenceDao persistenceDao=new PersistenceDaoMock(this.sessionFactory);
			def EntityMock entity=new EntityMock("id1","value1")
			
		when:
			persistenceDao.save(EntityMock.class,entity)
			
		then:
			this.currentRealSession.getRepo().get("id1").getValue().equals("value1")
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==true
			this.currentTransaction.hasBeenRollbacked()==false
			this.currentTransaction.isActive()==false
	}
	def "save entity with duplicated id should raise an exception and rollback the transaction"(){
		setup:
			def PersistenceDao persistenceDao=new PersistenceDaoMock(this.sessionFactory);
			def EntityMock entity=new EntityMock("id1","value1")
			def PersistenceSession session=persistenceDao.getSessionFactory().getSession();
			this.currentRealSession.persist(entity)
			session.close();
			
		when:
			persistenceDao.save(EntityMock.class,entity)
			
		then:
			def exception=thrown(DuplicateEntryException)
			exception.getMessage().equals(SimpleFormat.format(MESSAGE,EntityMock.class.getName(),"id1"))
			this.currentRealSession.getRepo().get("id1").getValue().equals("value1")
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==false
			this.currentTransaction.hasBeenRollbacked()==true
			this.currentTransaction.isActive()==false
	}
	def "contains entity should start and end transaction and persist the entity and return true if exist"(){
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
			this.currentTransaction.hasBeenCommited()==false
			this.currentTransaction.hasBeenRollbacked()==true
			this.currentTransaction.isActive()==false
	}

	def "get entity should start and end transaction and persist the entity"(){
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
			exception.getMessage().equals(SimpleFormat.format(MESSAGE,"get",EntityMock.class.getName(),"id2"))
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==false
			this.currentTransaction.hasBeenRollbacked()==true
			this.currentTransaction.isActive()==false
	}

	def "update entity should start and end transaction and merge the entity"(){
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
			def exception=thrown(UnknownEntryException)
			exception.getMessage().equals(SimpleFormat.format(MESSAGE,"update",EntityMock.class.getName(),"id2"))
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==false
			this.currentTransaction.hasBeenRollbacked()==true
			this.currentTransaction.isActive()==false
	}

	
	def "delete entity should start and end transaction and remove the entity"(){
		setup:
			def PersistenceDao persistenceDao=new PersistenceDaoMock(this.sessionFactory);
			def EntityMock entity=new EntityMock("id1","value1")
			def PersistenceSession session=persistenceDao.getSessionFactory().getSession();
			this.currentRealSession.persist(entity)
			session.close();
			
		when:
			persistenceDao.delete(EntityMock.class,entity)
			
		then:
			this.currentRealSession.getRepo().get("id1")==null
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==true
			this.currentTransaction.hasBeenRollbacked()==false
			this.currentTransaction.isActive()==false
	}
	def "delete entity of unknown id should raise an exception and rollback the transaction"(){
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
			def exception=thrown(UnknownEntryException)
			exception.getMessage().equals(SimpleFormat.format(MESSAGE,"delete",EntityMock.class.getName(),"id2"))
			this.currentRealSession.isClosed()==true
			this.currentTransaction.hasBeenStarted()==true
			this.currentTransaction.hasBeenCommited()==false
			this.currentTransaction.hasBeenRollbacked()==true
			this.currentTransaction.isActive()==false
	}
}

