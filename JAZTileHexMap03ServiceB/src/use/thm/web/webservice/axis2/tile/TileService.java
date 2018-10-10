package use.thm.web.webservice.axis2.tile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionFactoryImpl;

import basic.zBasic.ExceptionZZZ;
import basic.zBasic.KernelSingletonTHM;
import basic.zKernel.KernelZZZ;
import tryout.zBasic.persistence.webservice.TryoutSessionFactoryCreation;
import use.thm.persistence.dao.TileDefaulttextDao;
import use.thm.persistence.dao.TroopArmyDao;
import use.thm.persistence.hibernate.HibernateContextProviderJndiSingletonTHM;
import use.thm.persistence.hibernate.HibernateContextProviderSingletonTHM;
import use.thm.persistence.model.Key;
import use.thm.persistence.model.TileDefaulttext;
import use.thm.persistence.model.TroopArmy;
import use.thm.web.webservice.axis2.pojo.TileDefaulttextPojo;
import use.thm.web.webservice.axis2.pojo.TroopArmyPojo;

public class TileService{
	//Das funktoiniert wohl nur, wenn diese Ressource irgendwie bekannt gemacht worden ist.
	//@Resource
	//private WebServiceContext context; //Das soll durch die Annotation dieses Context Objekt zur Verfügung stellen.
	
	@Resource
	ServletContext context; //you can specify in your method argument
	//String realPath = context.getRealPath("/");
	
	public String getVersion(){
		String sVersion = "0.082";			
		return sVersion;
		
		/*
		 * 0.082: Hole den zu verwendenen JNDI-String aus der Kernel-Konfiguration. (Lies überhaupt erstmalig die Kernel Koniguration per WebService aus).
		 * 0.081: Einbau einer anderen SQLITE Version und eines anderen Dialekts, was entsprechend der SWING Applikation angepasst wurde.
		 * 
		 */
	}
	public String getNow(){
		Calendar cal = Calendar.getInstance();
		//Date date = new Date();
		Date date = cal.getTime();
		String sReturn = new Integer(date.getYear()).toString() + new Integer(date.getMonth()).toString() + new Integer(date.getDay()).toString();
		return sReturn;
	}
	
	/**Merke: Das ist der hartverdrahtete Einsatz der Methode.
	 *        In den WebServices kann durchaus eine Methode existieren, in der der JndiContext String übergeben wird.
	 *        Z.B.  HibernateCheckConfigurationServiceZZZ.getProofJndiResourceAvailable(sContextJndi);
	 * @param sJndiContext
	 * @return
	 */
	public String getProofJndiResourceUsedAvailable(){
		String sReturn=null;
		
		//Missbrauch dieser Methode:
		//Tryout eine SessionFactory per JNDI zu erzeugen
		TryoutSessionFactoryCreation objTryout = new TryoutSessionFactoryCreation();
		//Das funktioniert. boolean bReturn = objTryout.tryoutGetSessionFactoryByJndi();
		
		//DEBUG: 20171206 NEUE ALTERNATIVE ÜBER CONTEXTPROVIDERJNDI
		boolean bReturn = objTryout.tryoutGetSessionFactoryByJndiContextProvider();
		if(bReturn){
			sReturn = "vorhanden";
		}else{
			sReturn = "nicht vorhanden";
		}
		return sReturn;
	}
	
	/* Hier wird dann erstmalig ein Hibernate basiertes Objekt verwendet, aus einem anderen Projekt*/
	public Integer getTroopArmyCount(){
		Integer intReturn = null;					
		try {		
			//funktioniert, wenn dies Datei als .jar Datei in das lib-Verzeichnis des Servers gepackt wird.
//			WebDeploymentTest objTest = new WebDeploymentTest();
//			objTest.doIt();
//			intReturn = new Integer(0);
			
			//KernelZZZ objKernel = new KernelZZZ(); //Merke: Die Service Klasse selbst kann wohl nicht das KernelObjekt extenden!
			//20171207: Wurde die Konfiguration für JNDI wie für  nomaleJ2SE Anwendungen gebaut.
			//20171207: Version ohne JNDI HibernateContextProviderSingletonTHM objContextHibernate = HibernateContextProviderSingletonTHM.getInstance(objKernel);
			//                            Context jndiContext = (Context) new InitialContext();
			//                            SessionFactory sf = (SessionFactory) jndiContext.lookup("java:comp/env/jdbc/ServicePortal");
                              
//          Nun wird die Konfiguraton explizit auf die Angaben in der context.xml des Servers reduziert. Die SessionFactory per JNDI geholt. Anschliessend an meinen Context...Provider übergeben.
//			String sContextJndi = "jdbc/ServicePortal";
//			HibernateContextProviderJndiSingletonTHM objContextHibernate = HibernateContextProviderJndiSingletonTHM.getInstance(objKernel, sContextJndi);
		

			//TODO 20181005 wie den context hier injekten per annotations????
			//String s = context.getRealPath("TESTE");											
			//ServletContext servletContext = (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);

			//20181008: Lies die zu verwendende JNDI-Ressource aus der Kernelkoniguration aus.
			KernelSingletonTHM objKernelSingleton = KernelSingletonTHM.getInstance();
			String sDatabaseRemoteNameJNDI = objKernelSingleton.getParameter("DatabaseRemoteNameJNDI");
			HibernateContextProviderJndiSingletonTHM objContextHibernate = HibernateContextProviderJndiSingletonTHM.getInstance(objKernelSingleton, sDatabaseRemoteNameJNDI);
			
			objContextHibernate.getConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");  //! Jetzt erst wird jede Tabelle über den Anwendungsstart hinaus gespeichert UND auch wiedergeholt.				
			
			//Dafür ist es wichtig für JNDI: Die SessionFactory an den Context zu binden
			//objContextHibernate.getConfiguration().setProperty("hibernate.session_factory_name", "tryout.zBasic.persistence.hibernate.HibernateSessionFactoryTomcatFactory");
//			objContextHibernate.getConfiguration().setProperty("hibernate.session_factory_name","hibernate.session-factory.ServicePortal");	//derselbe Name wird dann in jndiContext.lookup(...) gebraucht.
//			objContextHibernate.getConfiguration().setProperty("hibernate.connection.datasource", "java:comp/env/jdbc/ServicePortal");//siehe context.xml <RessourceLink> Tag.  //Merke comp/env ist fester Bestandteil für alle JNDI Pfade					
			//SessionFactory sf = (SessionFactory) jndiContext.lookup("hibernate.session-factory.ServicePortal");

			//Mein Ansatz: Verwende eine eigene SessionFactory und nimm die erstellte Konfiguration (aus HibernateContextProviderTHM) weiterhin und überschreibe diese ggfs. aus der Konfiguration.
			//                   Die hier erzeugte SessionFactory wird dann in das ContextHibernateProviderTHM-Objekt gespeichert. Dadurch wird die SessionFactory nur einmal erzeugt.
			//Merke: Damit diese Resource bekannt ist im Web Service, muss er neu gebaut werden. Nur dann ist die web.xml aktuell genug.
			//Merke: java:comp/env/ ist der JNDI "Basis" Pfad, der vorangestellt werden muss. Das ist also falsch: //SessionFactory sf = (SessionFactory) jndiContext.lookup("java:jdbc/ServicePortal");
			//Merke: /jdbc/ServicePortal ist in der context.xml im <RessourceLink>-Tag definiert UND in der web.xml im <resource-env-ref>-Tag
												
			TroopArmyDao daoTroop = new TroopArmyDao(objContextHibernate);
			int iTroopCounted = daoTroop.count();
			System.out.println("Es gibt platzierte Armeen: " + iTroopCounted);
			
			intReturn = new Integer(iTroopCounted);
			
		    //Mache die Session und anschliessend alles wieder zu, inklusive der SessionFactory...
//			Session session = null;
//			if(sf!=null){
//				session = sf.openSession();
//				//.........
//				session.clear();
//				session.close();
//			    sf.close();
//			}
//		} catch (NamingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();			
		} catch (ExceptionZZZ e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return intReturn;
				
	}
	
	
	/* Hier wird dann erstmalig eine eigens dafür erstellte HQL Abfrage ausgeführt und das Ergebnis soll zurückgeliefert werden. */
	public List<TroopArmyPojo> getTroopArmiesByHexCell(String sMap, String sX, String sY){
		List<TroopArmyPojo> listReturn = null;	
		try {
			//HOLE DIE SESSIONFACTORY PER JNDI:
			//Merke: DAS FUNKTIONIERT NUR, WENN DIE ANWENDUNG IN EINEM SERVER (z.B. Tomcat läuft).
			
//			KernelZZZ objKernel = new KernelZZZ(); //Merke: Die Service Klasse selbst kann wohl nicht das KernelObjekt extenden!			
			//HibernateContextProviderSingletonTHM objContextHibernate = HibernateContextProviderSingletonTHM.getInstance(objKernel);
//			String sContextJndi = "jdbc/ServicePortal";
//			HibernateContextProviderJndiSingletonTHM objContextHibernate = HibernateContextProviderJndiSingletonTHM.getInstance(objKernel, sContextJndi);
			
			KernelSingletonTHM objKernelSingleton = KernelSingletonTHM.getInstance();
			String sDatabaseRemoteNameJNDI = objKernelSingleton.getParameter("DatabaseRemoteNameJNDI");
			HibernateContextProviderJndiSingletonTHM objContextHibernate = HibernateContextProviderJndiSingletonTHM.getInstance(objKernelSingleton, sDatabaseRemoteNameJNDI);
			
			objContextHibernate.getConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");  //! Jetzt erst wird jede Tabelle über den Anwendungsstart hinaus gespeichert UND auch wiedergeholt.				
			
			//############################
			//MERKE: DAS IST DER WEG wei bisher die SessionFactory direkt in einer Standalone J2SE Anwendung geholt wird
			//ServiceRegistry sr = new ServiceRegistryBuilder().applySettings(cfg.getProperties()).buildServiceRegistry();		    
		    //SessionFactory sf = cfg.buildSessionFactory(sr);
			//################################
		
			//xxxx Da wird mit normalen JDBC Datenbanenk als DataSource gearbeitet. Das ist mit Hibernate so nicht möglich
			//holds config elements
			//DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/yourdb");
			//Connection conn = ds.getConnection();
										
			//### Ansatz Session-Factory über die Utility Funktion zu holen, die dann in der Hibernate Konfiguration nachsieht.
			//1. Versuch: In der Hibernate Configuration definiert
			//    Fehler: SessionFactory creation failed! javax.naming.NoInitialContextException: Need to specify class name in environment or system property, or as an applet parameter, or in an application resource file:  java.naming.factory.initial
			
			//2. Versuch: In der Hibernate Configuration Erstellung per Java definiert
			//Die hier genannte SessionFactory muss tatsächlich als Klasse an der Stelle existieren.
										
			//3. Versuch:				
			//Betzemeier Original:  //SessionFactory sf = HibernateUtilByAnnotation.getHibernateUtil().getSessionFactory();
			//Betzemeier Original:  Hier wird JNDI für eine fest vorgegebeen Klasse verwendet. //SessionFactory sf = (SessionFactory) jndiContext.lookup("hibernate.session-factory.ServicePortal");
			
			//Mein Ansatz: Verwende eine eigene SessionFactory und nimm die erstellte Konfiguration (aus HibernateContextProviderTHM) weiterhin und überschreibe diese ggfs. aus der Konfiguration.
			//Merke: Damit diese Resource bekannt ist im Web Service, muss er neu gebaut werden. Nur dann ist die web.xml aktuell genug.
			//Merke: java:comp/env/ ist der JNDI "Basis" Pfad, der vorangestellt werden muss. Das ist also falsch: //SessionFactory sf = (SessionFactory) jndiContext.lookup("java:jdbc/ServicePortal");
			//Merke: /jdbc/ServicePortal ist in der context.xml im <RessourceLink>-Tag definiert UND in der web.xml im <resource-env-ref>-Tag
			
			//Wenn man die SessionFactory direkt per JNDI holt...
			//Context jndiContext = (Context) new InitialContext();
			//SessionFactory sf = (SessionFactory) jndiContext.lookup("java:comp/env/jdbc/ServicePortal");
			
			//Hole die SessionFactory für JNDI aus dem ContextProvider Objekt.
			SessionFactory sf = (SessionFactory) objContextHibernate.getSessionFactoryByJndi();
						
			TroopArmyDao daoTroop = new TroopArmyDao(objContextHibernate);
			List<TroopArmy>listTroopArmy = daoTroop.searchTileCollectionByHexCell(sMap, sX, sY);//.searchTileIdCollectionByHexCell(sMap, sX, sY);
			System.out.println("Es gibt auf der Karte '" + sMap + " an X/Y (" + sX + "/" + sY + ") platzierte Armeen: " + listTroopArmy.size());
			
			if(listTroopArmy.size()>=1){
				listReturn = new ArrayList<TroopArmyPojo>();
			}
			for(TroopArmy objTroop : listTroopArmy){
				TroopArmyPojo objPojo = new TroopArmyPojo();
				objPojo.setUniquename(objTroop.getUniquename());
				objPojo.setPlayer(new Integer(objTroop.getPlayer()));
				objPojo.setType(objTroop.getTroopType());
				
				//Der Vollständigkeit halber auch die Eingangswerte zurückgeben, sofern sie zum POJO gehören.
				objPojo.setMapAlias(sMap);
				objPojo.setMapX(objTroop.getMapX());
				objPojo.setMapY(objTroop.getMapY());				
				listReturn.add(objPojo);
			}		
		} catch (ExceptionZZZ e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listReturn;	
	}
	
	/* Hier wird dann erstmalig eine eigens dafür erstellte HQL Abfrage ausgeführt und das Ergebnis soll zurückgeliefert werden.
	 * MERKE: Der hier vergebenen Variablennamen ist dann der Name der Methode in der Input-Klasse des Webservice. Bei der Entwicklung des WebServiceClients.
	 *        z.B. sMap ==>  getTroopArmiesAll10.setSMap("EINS"); */
	public List<TroopArmyPojo> getTroopArmiesAll(String sMap){ //TODO: Irgendeine Sortierung als Parameter vorgeben...{
		List<TroopArmyPojo> listReturn = null;	
		try {
			//HOLE DIE SESSIONFACTORY PER JNDI:
			//Merke: DAS FUNKTIONIERT NUR, WENN DIE ANWENDUNG IN EINEM SERVER (z.B. Tomcat läuft).
			
			//KernelZZZ objKernel = new KernelZZZ(); //Merke: Die Service Klasse selbst kann wohl nicht das KernelObjekt extenden!			
			//HibernateContextProviderSingletonTHM objContextHibernate = HibernateContextProviderSingletonTHM.getInstance(objKernel);
			//String sContextJndi = "jdbc/ServicePortal";
			//HibernateContextProviderJndiSingletonTHM objContextHibernate = HibernateContextProviderJndiSingletonTHM.getInstance(objKernel, sContextJndi);
			
			KernelSingletonTHM objKernelSingleton = KernelSingletonTHM.getInstance();
			String sDatabaseRemoteNameJNDI = objKernelSingleton.getParameter("DatabaseRemoteNameJNDI");
						
			HibernateContextProviderJndiSingletonTHM objContextHibernate = HibernateContextProviderJndiSingletonTHM.getInstance(objKernelSingleton, sDatabaseRemoteNameJNDI);
			objContextHibernate.getConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");  //! Jetzt erst wird jede Tabelle über den Anwendungsstart hinaus gespeichert UND auch wiedergeholt.				
			
			//############################
			//MERKE: DAS IST DER WEG wei bisher die SessionFactory direkt in einer Standalone J2SE Anwendung geholt wird
			//ServiceRegistry sr = new ServiceRegistryBuilder().applySettings(cfg.getProperties()).buildServiceRegistry();		    
		    //SessionFactory sf = cfg.buildSessionFactory(sr);
			//################################
		
			//xxxx Da wird mit normalen JDBC Datenbanenk als DataSource gearbeitet. Das ist mit Hibernate so nicht möglich
			//holds config elements
			//DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/yourdb");
			//Connection conn = ds.getConnection();
										
			//### Ansatz Session-Factory über die Utility Funktion zu holen, die dann in der Hibernate Konfiguration nachsieht.
			//1. Versuch: In der Hibernate Configuration definiert
			//    Fehler: SessionFactory creation failed! javax.naming.NoInitialContextException: Need to specify class name in environment or system property, or as an applet parameter, or in an application resource file:  java.naming.factory.initial
			
			//2. Versuch: In der Hibernate Configuration Erstellung per Java definiert
			//Die hier genannte SessionFactory muss tatsächlich als Klasse an der Stelle existieren.
										
			//3. Versuch:				
			//Betzemeier Original:  //SessionFactory sf = HibernateUtilByAnnotation.getHibernateUtil().getSessionFactory();
			//Betzemeier Original:  Hier wird JNDI für eine fest vorgegebeen Klasse verwendet. //SessionFactory sf = (SessionFactory) jndiContext.lookup("hibernate.session-factory.ServicePortal");
			
			//Mein Ansatz: Verwende eine eigene SessionFactory und nimm die erstellte Konfiguration (aus HibernateContextProviderTHM) weiterhin und überschreibe diese ggfs. aus der Konfiguration.
			//Merke: Damit diese Resource bekannt ist im Web Service, muss er neu gebaut werden. Nur dann ist die web.xml aktuell genug.
			//Merke: java:comp/env/ ist der JNDI "Basis" Pfad, der vorangestellt werden muss. Das ist also falsch: //SessionFactory sf = (SessionFactory) jndiContext.lookup("java:jdbc/ServicePortal");
			//Merke: /jdbc/ServicePortal ist in der context.xml im <RessourceLink>-Tag definiert UND in der web.xml im <resource-env-ref>-Tag
			
			//Wenn man die SessionFactory direkt per JNDI holt...
			//Context jndiContext = (Context) new InitialContext();
			//SessionFactory sf = (SessionFactory) jndiContext.lookup("java:comp/env/jdbc/ServicePortal");
			
			//Hole die SessionFactory für JNDI aus dem ContextProvider Objekt.
			SessionFactory sf = (SessionFactory) objContextHibernate.getSessionFactoryByJndi();
						
			TroopArmyDao daoTroop = new TroopArmyDao(objContextHibernate);
			List<TroopArmy>listTroopArmy = daoTroop.searchTroopArmiesAll(sMap);			
			if(listTroopArmy.size()>=1){
				System.out.println("Es gibt auf der Karte '" + sMap + " platzierte Armeen: " + listTroopArmy.size());				
				listReturn = new ArrayList<TroopArmyPojo>();
			}
			for(TroopArmy objTroop : listTroopArmy){
				TroopArmyPojo objPojo = new TroopArmyPojo();
				objPojo.setUniquename(objTroop.getUniquename());
				objPojo.setPlayer(new Integer(objTroop.getPlayer()));
				objPojo.setType(objTroop.getTroopType());
				
				objPojo.setMapAlias(sMap);
							
				objPojo.setMapX(new Integer(objTroop.getMapX()));
				objPojo.setMapY(new Integer(objTroop.getMapY()));
				
				listReturn.add(objPojo);
			}
		} catch (ExceptionZZZ e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listReturn;	
	}
	
	
	/* Hier wird per DAO der Defaulttext für eine Army geholt. Dabei wird der (momentan noch) der Thiskey direkt angegeben.
	 * TODO GOON 20171115: Das soll eigentlich über eine noch zu erstellende Armeetyp - Tabelle passieren, in welcher der thiskey abgelegt ist.
	 */
	public TileDefaulttextPojo getTileDefaulttextByThiskey(Long lngThiskey){
		TileDefaulttextPojo objReturn = null;	
		try {
			//HOLE DIE SESSIONFACTORY PER JNDI:
			//Merke: DAS FUNKTIONIERT NUR, WENN DIE ANWENDUNG IN EINEM SERVER (z.B. Tomcat läuft).
			
			//KernelZZZ objKernel = new KernelZZZ(); //Merke: Die Service Klasse selbst kann wohl nicht das KernelObjekt extenden!				
			//HibernateContextProviderSingletonTHM objContextHibernate = HibernateContextProviderSingletonTHM.getInstance(objKernel);					
			
			//String sContextJndi = "jdbc/ServicePortal";
			
			KernelSingletonTHM objKernelSingleton = KernelSingletonTHM.getInstance();
			String sDatabaseRemoteNameJNDI = objKernelSingleton.getParameter("DatabaseRemoteNameJNDI");
			
			HibernateContextProviderJndiSingletonTHM objContextHibernate = HibernateContextProviderJndiSingletonTHM.getInstance(objKernelSingleton, sDatabaseRemoteNameJNDI);
			objContextHibernate.getConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");  //! Jetzt erst wird jede Tabelle über den Anwendungsstart hinaus gespeichert UND auch wiedergeholt.				
			
			//############################
			//MERKE: DAS IST DER WEG wei bisher die SessionFactory direkt in einer Standalone J2SE Anwendung geholt wird
			//ServiceRegistry sr = new ServiceRegistryBuilder().applySettings(cfg.getProperties()).buildServiceRegistry();		    
		    //SessionFactory sf = cfg.buildSessionFactory(sr);
			//################################
		
			//xxxx Da wird mit normalen JDBC Datenbanenk als DataSource gearbeitet. Das ist mit Hibernate so nicht möglich
			//holds config elements
			//DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/yourdb");
			//Connection conn = ds.getConnection();
										
			//### Ansatz Session-Factory über die Utility Funktion zu holen, die dann in der Hibernate Konfiguration nachsieht.
			//1. Versuch: In der Hibernate Configuration definiert
			//    Fehler: SessionFactory creation failed! javax.naming.NoInitialContextException: Need to specify class name in environment or system property, or as an applet parameter, or in an application resource file:  java.naming.factory.initial
			
			//2. Versuch: In der Hibernate Configuration Erstellung per Java definiert
			//Die hier genannte SessionFactory muss tatsächlich als Klasse an der Stelle existieren.
										
			//3. Versuch:				
			//Betzemeier Original:  //SessionFactory sf = HibernateUtilByAnnotation.getHibernateUtil().getSessionFactory();
			//Betzemeier Original:  Hier wird JNDI für eine fest vorgegebeen Klasse verwendet. //SessionFactory sf = (SessionFactory) jndiContext.lookup("hibernate.session-factory.ServicePortal");
			
			//Mein Ansatz: Verwende eine eigene SessionFactory und nimm die erstellte Konfiguration (aus HibernateContextProviderTHM) weiterhin und überschreibe diese ggfs. aus der Konfiguration.
			//Merke: Damit diese Resource bekannt ist im Web Service, muss er neu gebaut werden. Nur dann ist die web.xml aktuell genug.
			//Merke: java:comp/env/ ist der JNDI "Basis" Pfad, der vorangestellt werden muss. Das ist also falsch: //SessionFactory sf = (SessionFactory) jndiContext.lookup("java:jdbc/ServicePortal");
			//Merke: /jdbc/ServicePortal ist in der context.xml im <RessourceLink>-Tag definiert UND in der web.xml im <resource-env-ref>-Tag
			
			//Wenn man die SessionFactory direkt per JNDI holt...
			//Context jndiContext = (Context) new InitialContext();
			//SessionFactory sf = (SessionFactory) jndiContext.lookup("java:comp/env/jdbc/ServicePortal");
			
			//Hole die SessionFactory für JNDI aus dem ContextProvider Objekt.
			SessionFactory sf = (SessionFactory) objContextHibernate.getSessionFactoryByJndi();
							
			TileDefaulttextDao daoText = new TileDefaulttextDao(objContextHibernate);
			Key objKey = daoText.searchThiskey(lngThiskey);
			if(objKey==null){
				System.out.println("Thiskey='"+lngThiskey.toString()+"' NICHT gefunden.");
			}else{
				TileDefaulttext objValue = (TileDefaulttext) objKey;
				
				String sDescription = objValue.getDescription();
				String sShorttext = objValue.getShorttext();				
				String sLongtext = objValue.getLongtext();
				
				System.out.println("Thiskey='"+lngThiskey.toString()+"' gefunden. ("+sShorttext+"|"+sLongtext+"|"+sDescription+")");
				
				//### Übergib nun die gefundenen Werte an das POJO - Objekt
				objReturn = new TileDefaulttextPojo();
				objReturn.setThiskey(lngThiskey);
				objReturn.setShorttext(sShorttext);
				objReturn.setLongtext(sLongtext);
				objReturn.setDescriptiontext(sDescription);
			}													
		} catch (ExceptionZZZ e) {
			// TODO Auto-generated catch block
			e.printStackTrace();		
		}
		return objReturn;
				
	}
}
