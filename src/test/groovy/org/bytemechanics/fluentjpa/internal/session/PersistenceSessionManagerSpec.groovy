package org.bytemechanics.fluentjpa.internal.session

import org.bytemechanics.fluentjpa.internal.session.*
import org.bytemechanics.fluentjpa.*
import org.bytemechanics.fluentjpa.mocks.*
import org.bytemechanics.fluentjpa.exceptions.*
import java.util.logging.*
import java.io.*
import spock.lang.*
import spock.mock.*
import javax.persistence.*

/**
 * @author afarre
 */
class PersistenceSessionManagerSpec extends Specification {

	def setupSpec(){
		println(">>>>> PersistenceSessionManagerSpec >>>>  setupSpec")

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
	
	def EntityManagerFactory entityManagerFactory

	def setup(){
		println(">>>>> PersistenceSessionManagerSpec >>>> setup")
		this.entityManagerFactory=Mock(EntityManagerFactory)
	}
	def cleanup(){
		println(">>>>> PersistenceSessionManagerSpec >>>> cleanup")
		this.entityManagerFactory=null
	}
	
	def "create manager without factory should launch an exception"(){
		println(">>>>> PersistenceSessionManagerSpec >>>>  create manager without factory should launch an exception")

		when:
			new PersistenceSessionManager(null,{properties -> return null})

		then:
			thrown(MandatoryParameterException)
	}

	def "create session from an empty supplier should launch an exception"(){
		println(">>>>> PersistenceSessionManagerSpec >>>>  create session from an empty supplier should launch an exception")

		setup:
			def PersistenceSessionManager sessionManager=new PersistenceSessionManager(entityManagerFactory,{properties -> return null})
			sessionManager.init()
			def PersistenceSession session

		when:
			session=sessionManager.getSession()	

		then:
			thrown(PersistenceSessionSupplierException)
		
		cleanup:
			sessionManager.close()
	}

	def "two consequent sessions from the same thread should return distinct session"(){
		println(">>>>> PersistenceSessionManagerSpec >>>>  two consequent sessions from the same thread should return distinct session")

		setup:
			def PersistenceSessionManager sessionManager=new PersistenceSessionManager(entityManagerFactory,{properties -> return new EntitySessionMock()})
			sessionManager.init()
			def PersistenceSession session1
			def PersistenceSession session2

		when:
			session1=sessionManager.getSession()
			session1.close()
			session2=sessionManager.getSession()

		then:
			session1!=session2
			session1 instanceof PersistenceSessionMasterImpl
			session2 instanceof PersistenceSessionMasterImpl

		cleanup:
			sessionManager.close()
	}
	def "two anidated sessions from the same thread should return one master and one slave"(){
		println(">>>>> PersistenceSessionManagerSpec >>>>  two anidated sessions from the same thread should return one master and one slave")

		setup:
			def PersistenceSessionManager sessionManager=new PersistenceSessionManager(entityManagerFactory,{properties -> return new EntitySessionMock()})
			sessionManager.init()
			def PersistenceSession session1
			def PersistenceSession session2

		when:
			session1=sessionManager.getSession()
			session2=sessionManager.getSession()

		then:
			session1!=session2
			session1 instanceof PersistenceSessionMasterImpl
			session2 instanceof PersistenceSessionSlaveImpl
			session1==session2.session

		cleanup:
			sessionManager.close()
	}
	def "two sessions from the distinct thread should return the distinct session"(){
		println(">>>>> PersistenceSessionManagerSpec >>>>  two sessions from the distinct thread should return the distinct session")

		setup:
			def final PersistenceSessionManager sessionManager=new PersistenceSessionManager(entityManagerFactory,{properties -> return new EntitySessionMock()})
			sessionManager.init()
			def PersistenceSession session1
			def PersistenceSession session2

		when:
			session1=sessionManager.getSession()
			def Thread alternateThread=Thread.start{session2=sessionManager.getSession()}
			alternateThread.join()	

		then:
			session1!=session2
			session1 instanceof PersistenceSessionMasterImpl
			session2 instanceof PersistenceSessionMasterImpl

		cleanup:
			sessionManager.close()
	}
}

