package org.bytemechanics.fluentjpa

import spock.lang.*
import javax.persistence.EntityManager
import org.bytemechanics.fluentjpa.*
import org.bytemechanics.fluentjpa.internal.session.*
import org.bytemechanics.fluentjpa.mocks.*
import java.util.logging.*
import java.io.*
import javax.persistence.*
import spock.mock.*



/**
 * @author afarre
 */
class PersistenceSessionSpec extends Specification {

	def setupSpec(){
		println(">>>>> PersistenceSessionSpec >>>>  setupSpec")

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
	def EntitySessionMock currentRealSession
	def EntityManagerFactory entityManagerFactory
	
	def setup(){
		println(">>>>> PersistenceSessionSpec >>>>  setup")
		this.entityManagerFactory=Mock(EntityManagerFactory)	
		EntitySessionMock.cleanup()
		sessionManager=new PersistenceSessionManager(this.entityManagerFactory,
			{properties -> 
				currentRealSession=new EntitySessionMock() 
				return currentRealSession
			})
	}
	def cleanup(){
		println(">>>>> PersistenceSessionSpec >>>>  cleanup")
		entityManagerFactory=null
		sessionManager=null
		currentRealSession=null
	}
	
	def "create first session must left an open session with isEmpty repository"(){
		println(">>>>> PersistenceSessionSpec >>>>  create first session must left an open session with isEmpty repository")

		setup:
			def PersistenceSession persitenceSession
		
		when:
			persitenceSession=sessionManager.getSession()
			
		then:
			persitenceSession!=null
			persitenceSession instanceof PersistenceSessionMasterImpl
			persitenceSession.isOpen()==true
			currentRealSession.isOpen()==true
			currentRealSession.getRepo().isEmpty()==true
			sessionManager.hasSession()==true
	}
	def "add entity to first session must left repository with one entity"(){
		println(">>>>> PersistenceSessionSpec >>>>  add entity to first session must left repository with one entity")

		setup:
			def PersistenceSession persitenceSession=sessionManager.getSession()
			def EntityMock entity=new EntityMock("myEntity","myEntityValue")
		
		when:
			persitenceSession.persist(entity)	
			
		then:
			persitenceSession!=null
			persitenceSession.isOpen()==true
			persitenceSession instanceof PersistenceSessionMasterImpl
			currentRealSession.isOpen()==true
			currentRealSession.getRepo().isEmpty()==false
			currentRealSession.getRepo().get(entity.getId())==entity
			sessionManager.hasSession()==true
	}
	def "close first session must left session closed but with repository changes"(){
		println(">>>>> PersistenceSessionSpec >>>>  close first session must left session closed but with repository changes")

		setup:
			def PersistenceSession persitenceSession=sessionManager.getSession()
			def EntityMock entity=new EntityMock("myEntity","myEntityValue")
			persitenceSession.persist(entity)
		
		when:
			persitenceSession.close()	
			
		then:
			persitenceSession!=null
			persitenceSession.isOpen()==false
			persitenceSession instanceof PersistenceSessionMasterImpl
			currentRealSession.isOpen()==false
			currentRealSession.getRepo().isEmpty()==false
			currentRealSession.getRepo().get(entity.getId())==entity
			sessionManager.hasSession()==false
	}

	def "create second session must left the session active like the first but with the repository changes applied"(){
		println(">>>>> PersistenceSessionSpec >>>>  create second session must left the session active like the first but with the repository changes applied")

		setup:
			def PersistenceSession persitenceSession=sessionManager.getSession()
			def EntityMock entity=new EntityMock("myEntity","myEntityValue")
			persitenceSession.persist(entity)
			persitenceSession.close()
		
		when:
			persitenceSession=sessionManager.getSession()
			
		then:
			persitenceSession!=null
			persitenceSession.isOpen()==true
			persitenceSession instanceof PersistenceSessionMasterImpl
			currentRealSession.isOpen()==true
			currentRealSession.getRepo().isEmpty()==false
			currentRealSession.getRepo().get(entity.getId())==entity
			sessionManager.hasSession()==true
	}
	def "add another entity to second session must left the session active and two repository objects"(){
		println(">>>>> PersistenceSessionSpec >>>>  add another entity to second session must left the session active and two repository objects")

		setup:
			def PersistenceSession persitenceSession=sessionManager.getSession()
			def EntityMock entity=new EntityMock("myEntity","myEntityValue")
			persitenceSession.persist(entity)
			persitenceSession.close()
			def EntityMock entity2=new EntityMock("myEntity2","myEntityValue2")
			persitenceSession=sessionManager.getSession()
		
		when:
			persitenceSession.persist(entity2)
			
		then:
			persitenceSession!=null
			persitenceSession.isOpen()==true
			persitenceSession instanceof PersistenceSessionMasterImpl
			currentRealSession.isOpen()==true
			currentRealSession.getRepo().isEmpty()==false
			currentRealSession.getRepo().get(entity.getId())==entity
			currentRealSession.getRepo().get(entity2.getId())==entity2
			sessionManager.hasSession()==true
	}

	def "close second session must left the session inactive but with the two entities at repository"(){
		println(">>>>> PersistenceSessionSpec >>>>  close second session must left the session inactive but with the two entities at repository")

		setup:
			def PersistenceSession persitenceSession=sessionManager.getSession()
			def EntityMock entity=new EntityMock("myEntity","myEntityValue")
			persitenceSession.persist(entity)
			persitenceSession.close()
			persitenceSession=sessionManager.getSession()
			def EntityMock entity2=new EntityMock("myEntity2","myEntityValue2")
			persitenceSession.persist(entity2)

		when:
			persitenceSession.close()
			
		then:
			persitenceSession!=null
			persitenceSession.isOpen()==false
			persitenceSession instanceof PersistenceSessionMasterImpl
			currentRealSession.isOpen()==false
			currentRealSession.getRepo().isEmpty()==false
			currentRealSession.getRepo().get(entity.getId())==entity
			currentRealSession.getRepo().get(entity2.getId())==entity2
			sessionManager.hasSession()==false
	}
	
	def "create anidated session must have not side effects and should see the real repo"(){
		println(">>>>> PersistenceSessionSpec >>>>  create anidated session must have not side effects and should see the real repo")

		setup:
			def PersistenceSession persitenceSession=sessionManager.getSession()
			def EntityMock entity=new EntityMock("myEntity","myEntityValue")
			persitenceSession.persist(entity)
		
		when:
			persitenceSession=sessionManager.getSession()
			
		then:
			persitenceSession!=null
			persitenceSession.isOpen()==true
			persitenceSession instanceof PersistenceSessionSlaveImpl
			currentRealSession.isOpen()==true
			currentRealSession.getRepo().isEmpty()==false
			currentRealSession.getRepo().get(entity.getId())==entity
			sessionManager.hasSession()==true
	}
	
	def "persist an additional entity to anidated session must add to the original repo"(){
		println(">>>>> PersistenceSessionSpec >>>>  persist an additional entity to anidated session must add to the original repo")

		setup:
			def PersistenceSession persitenceSession=sessionManager.getSession()
			def EntityMock entity=new EntityMock("myEntity","myEntityValue")
			persitenceSession.persist(entity)
			persitenceSession=sessionManager.getSession()
			def EntityMock entity2=new EntityMock("myEntity2","myEntityValue2")

		when:
			persitenceSession.persist(entity2)
			
		then:
			persitenceSession!=null
			persitenceSession.isOpen()==true
			persitenceSession instanceof PersistenceSessionSlaveImpl
			currentRealSession.isOpen()==true
			currentRealSession.getRepo().isEmpty()==false
			currentRealSession.getRepo().get(entity.getId())==entity
			currentRealSession.getRepo().get(entity2.getId())==entity2
			sessionManager.hasSession()==true
	}
	def "close anidated session must have not side effects, keep the original session open and the given closed and keep the repo status"(){
		println(">>>>> PersistenceSessionSpec >>>>  close anidated session must have not side effects, keep the original session open and the given closed and keep the repo status")

		setup:
			def PersistenceSession persitenceSession=sessionManager.getSession()
			def EntityMock entity=new EntityMock("myEntity","myEntityValue")
			persitenceSession.persist(entity)
			def anidatedPersitenceSession=sessionManager.getSession()
			def EntityMock entity2=new EntityMock("myEntity2","myEntityValue2")
			anidatedPersitenceSession.persist(entity2)

		when:
			anidatedPersitenceSession.close()
			
		then:
			persitenceSession.isOpen()==true
			persitenceSession instanceof PersistenceSessionMasterImpl
			persitenceSession.toString()
			currentRealSession.isOpen()==true
			currentRealSession.getRepo().isEmpty()==false
			currentRealSession.getRepo().get(entity.getId())==entity
			currentRealSession.getRepo().get(entity2.getId())==entity2
			sessionManager.hasSession()==true
			anidatedPersitenceSession instanceof PersistenceSessionSlaveImpl
			anidatedPersitenceSession.isOpen()==false
			anidatedPersitenceSession.toString()
	}
}

