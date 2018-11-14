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

import basic.persistence.daoFacade.GeneralDaoFacadeZZZ;
import basic.zBasic.ExceptionZZZ;
import basic.zBasic.KernelSingletonTHM;
import basic.zBasic.ReflectCodeZZZ;
import basic.zBasic.persistence.interfaces.IHibernateContextProviderZZZ;
import basic.zBasic.util.datatype.string.StringZZZ;
import basic.zKernel.KernelZZZ;
import tryout.zBasic.persistence.webservice.TryoutSessionFactoryCreation;
import use.thm.persistence.dao.AreaCellDao;
import use.thm.persistence.dao.TileDefaulttextDao;
import use.thm.persistence.dao.TroopArmyDao;
import use.thm.persistence.dao.TroopArmyVariantDao;
import use.thm.persistence.dao.TroopDao;
import use.thm.persistence.dao.TroopFleetDao;
import use.thm.persistence.dao.TroopVariantDao;
import use.thm.persistence.dao.TroopVariantDaoFactory;
import use.thm.persistence.daoFacade.TileDaoFacade;
import use.thm.persistence.daoFacade.TileDaoFacadeFactoryTHM;
import use.thm.persistence.hibernate.HibernateContextProviderJndiSingletonTHM;
import use.thm.persistence.hibernate.HibernateContextProviderSingletonTHM;
import use.thm.persistence.model.AreaCell;
import use.thm.persistence.model.CellId;
import use.thm.persistence.model.Key;
import use.thm.persistence.model.TileDefaulttext;
import use.thm.persistence.model.Troop;
import use.thm.persistence.model.TroopArmy;
import use.thm.persistence.model.TroopArmyVariant;
import use.thm.persistence.model.TroopFleet;
import use.thm.persistence.model.TroopVariant;
import use.thm.persistence.util.HibernateUtilTHM;
import use.thm.web.webservice.axis2.pojo.TileDefaulttextPojo;
import use.thm.web.webservice.axis2.pojo.TroopArmyPojo;
import use.thm.web.webservice.axis2.pojo.TroopFleetPojo;

public class TileService{
	//Das funktioniert wohl nur, wenn diese Ressource irgendwie bekannt gemacht worden ist.
	//@Resource
	//private WebServiceContext context; //Das soll durch die Annotation dieses Context Objekt zur Verfügung stellen.
	
	@Resource
	ServletContext context; //you can specify in your method argument
	//String realPath = context.getRealPath("/");
	
	public String getVersion(){
		String sVersion = "0.10";			
		return sVersion;
		
		/*
		 * 0.100: JNDI wie Standalone verwenden. Die Differnzierung wird in einer Utility-Klasse gemacht.
		 * 0.091: Einfügen eines neuen Spielsteins.
		 * 0.090: Löschen eines Spielsteins, per UniqueName.
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
	
	/**Merke1: Das ist der hartverdrahtete Einsatz der Methode.
	 *         In den WebServices kann durchaus eine Methode existieren, in der der JndiContext String übergeben wird.
	 *         Z.B.  HibernateCheckConfigurationServiceZZZ.getProofJndiResourceAvailable(sContextJndi);
	 * Merke 2: Seit 20181020 gibt es auch ein JAZKernelServiceZZZ Projekt, in dem dies ohne Hibernate-Klassen gelöst wurde.
	 * @param sJndiContext
	 * @return
	 */
	public String proofJndiResourceUsedAvailable(){
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
			IHibernateContextProviderZZZ objHibernateContext = HibernateUtilTHM.getHibernateContextProviderUsed(objKernelSingleton);
			objHibernateContext.getConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");  //! Jetzt erst wird jede Tabelle über den Anwendungsstart hinaus gespeichert UND auch wiedergeholt.				
			
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
												
			TroopArmyDao daoTroop = new TroopArmyDao(objHibernateContext);
			int iTroopCounted = daoTroop.count();
			System.out.println("###############################################################################");
			System.out.println("Es gibt platzierte Armeen: " + iTroopCounted);
			System.out.println("###############################################################################");
			
			intReturn = new Integer(iTroopCounted);
			
			//Nach dem Update soll mit dem UI weitergearbeitet werden können			
			objHibernateContext.closeAll();
			System.out.println("SessionFactory über den HibernateContextProvider geschlossen.... Nun wieder bearbeitbar im Java Swing Client?");			
		} catch (ExceptionZZZ e) {
			System.out.println(e.getDetailAllLast());
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
			KernelSingletonTHM objKernelSingleton = KernelSingletonTHM.getInstance();	
			IHibernateContextProviderZZZ objHibernateContext = HibernateUtilTHM.getHibernateContextProviderUsed(objKernelSingleton);
			objHibernateContext.getConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");  //! Jetzt erst wird jede Tabelle über den Anwendungsstart hinaus gespeichert UND auch wiedergeholt.				
			
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
			//SessionFactory sf = (SessionFactory) objHibernateContext.getSessionFactory(); //ByJndi();
						
			TroopArmyDao daoTroop = new TroopArmyDao(objHibernateContext);
			List<TroopArmy>listTroopArmy = daoTroop.searchTileCollectionByHexCell(sMap, sX, sY);//.searchTileIdCollectionByHexCell(sMap, sX, sY);
			
			System.out.println("###############################################################################");
			System.out.println("Es gibt auf der Karte '" + sMap + " an X/Y (" + sX + "/" + sY + ") platzierte Armeen: " + listTroopArmy.size());
			System.out.println("###############################################################################");
			
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
			
			//Nach dem Update soll mit dem UI weitergearbeitet werden können			
			objHibernateContext.closeAll();
			System.out.println("SessionFactory über den HibernateContextProvider geschlossen.... Nun wieder bearbeitbar im Java Swing Client?");
		} catch (ExceptionZZZ e){
			System.out.println(e.getDetailAllLast());
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
			KernelSingletonTHM objKernelSingleton = KernelSingletonTHM.getInstance();	
			IHibernateContextProviderZZZ objHibernateContext = HibernateUtilTHM.getHibernateContextProviderUsed(objKernelSingleton);
			objHibernateContext.getConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");  //! Jetzt erst wird jede Tabelle über den Anwendungsstart hinaus gespeichert UND auch wiedergeholt.				
			
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
			//SessionFactory sf = (SessionFactory) objHibernateContext.getSessionFactory(); //ByJndi();
						
			TroopArmyDao daoTroop = new TroopArmyDao(objHibernateContext);
			List<TroopArmy>listTroopArmy = daoTroop.searchTroopArmiesAll(sMap);			
			if(listTroopArmy.size()>=1){
				System.out.println("###############################################################################");
				System.out.println("Es gibt auf der Karte '" + sMap + " platzierte Armeen: " + listTroopArmy.size());
				System.out.println("###############################################################################");
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
			
			//Nach dem Update soll mit dem UI weitergearbeitet werden können			
			objHibernateContext.closeAll();
			System.out.println("SessionFactory über den HibernateContextProvider geschlossen.... Nun wieder bearbeitbar im Java Swing Client?");
		} catch (ExceptionZZZ e){
			System.out.println(e.getDetailAllLast());
			e.printStackTrace();
		}
		return listReturn;	
	}
	
	/* Hier wird dann erstmalig eine eigens dafür erstellte HQL Abfrage ausgeführt und das Ergebnis soll zurückgeliefert werden.
	 * MERKE: Der hier vergebenen Variablennamen ist dann der Name der Methode in der Input-Klasse des Webservice. Bei der Entwicklung des WebServiceClients.
	 *        z.B. sMap ==>  getTroopArmiesAll10.setSMap("EINS"); */
	public List<TroopFleetPojo> getTroopFleetsAll(String sMap){ //TODO: Irgendeine Sortierung als Parameter vorgeben...{
		List<TroopFleetPojo> listReturn = null;	
		try {
			//HOLE DIE SESSIONFACTORY PER JNDI:
			//Merke: DAS FUNKTIONIERT NUR, WENN DIE ANWENDUNG IN EINEM SERVER (z.B. Tomcat läuft).
			KernelSingletonTHM objKernelSingleton = KernelSingletonTHM.getInstance();	
			IHibernateContextProviderZZZ objHibernateContext = HibernateUtilTHM.getHibernateContextProviderUsed(objKernelSingleton);
			objHibernateContext.getConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");  //! Jetzt erst wird jede Tabelle über den Anwendungsstart hinaus gespeichert UND auch wiedergeholt.				
			
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
			//SessionFactory sf = (SessionFactory) objHibernateContext.getSessionFactory(); //ByJndi();
						
			TroopFleetDao daoTroop = new TroopFleetDao(objHibernateContext);
			List<TroopFleet>listTroopFleet = daoTroop.searchTroopFleetsAll(sMap);			
			if(listTroopFleet.size()>=1){
				System.out.println("###############################################################################");
				System.out.println("Es gibt auf der Karte '" + sMap + " platzierte Flotten: " + listTroopFleet.size());
				System.out.println("###############################################################################");
				listReturn = new ArrayList<TroopFleetPojo>();
			}
			for(TroopFleet objTroop : listTroopFleet){
				TroopFleetPojo objPojo = new TroopFleetPojo();
				objPojo.setUniquename(objTroop.getUniquename());
				objPojo.setPlayer(new Integer(objTroop.getPlayer()));
				objPojo.setType(objTroop.getTroopType());
				
				objPojo.setMapAlias(sMap);
							
				objPojo.setMapX(new Integer(objTroop.getMapX()));
				objPojo.setMapY(new Integer(objTroop.getMapY()));
				
				listReturn.add(objPojo);
			}
			
			//Nach dem Update soll mit dem UI weitergearbeitet werden können			
			objHibernateContext.closeAll();
			System.out.println("SessionFactory über den HibernateContextProvider geschlossen.... Nun wieder bearbeitbar im Java Swing Client?");
		} catch (ExceptionZZZ e){
			System.out.println(e.getDetailAllLast());
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
			KernelSingletonTHM objKernelSingleton = KernelSingletonTHM.getInstance();	
			IHibernateContextProviderZZZ objHibernateContext = HibernateUtilTHM.getHibernateContextProviderUsed(objKernelSingleton);			
			objHibernateContext.getConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");  //! Jetzt erst wird jede Tabelle über den Anwendungsstart hinaus gespeichert UND auch wiedergeholt.				
			
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
			//SessionFactory sf = (SessionFactory) objHibernateContext.getSessionFactoryByJndi();
			//SessionFactory sf = (SessionFactory) objHibernateContext.getSessionFactory();
							
			TileDefaulttextDao daoText = new TileDefaulttextDao(objHibernateContext);
			Key objKey = daoText.searchThiskey(lngThiskey);
			if(objKey==null){
				System.out.println("###############################################################################");
				System.out.println("Thiskey='"+lngThiskey.toString()+"' NICHT gefunden.");
				System.out.println("###############################################################################");
			}else{
				TileDefaulttext objValue = (TileDefaulttext) objKey;
				
				String sDescription = objValue.getDescription();
				String sShorttext = objValue.getShorttext();				
				String sLongtext = objValue.getLongtext();
				
				System.out.println("###############################################################################");
				System.out.println("Thiskey='"+lngThiskey.toString()+"' gefunden. ("+sShorttext+"|"+sLongtext+"|"+sDescription+")");
				System.out.println("###############################################################################");
				
				//### Übergib nun die gefundenen Werte an das POJO - Objekt
				objReturn = new TileDefaulttextPojo();
				objReturn.setThiskey(lngThiskey);
				objReturn.setShorttext(sShorttext);
				objReturn.setLongtext(sLongtext);
				objReturn.setDescriptiontext(sDescription);
			}	
			
			//Nach dem Update soll mit dem UI weitergearbeitet werden können			
			objHibernateContext.closeAll();
			System.out.println("SessionFactory über den HibernateContextProvider geschlossen.... Nun wieder bearbeitbar im Java Swing Client?");
		} catch (ExceptionZZZ e) {
			System.out.println(e.getDetailAllLast());
			e.printStackTrace();		
		}
		return objReturn;
				
	}
	
	public String deleteTile(String sUniqueName){
		String sReturn = null;
		try{
			main:{				
				if(StringZZZ.isEmpty(sUniqueName)) break main;
				
				//HOLE DIE SESSIONFACTORY PER JNDI:
				//Merke: DAS FUNKTIONIERT NUR, WENN DIE ANWENDUNG IN EINEM SERVER (z.B. Tomcat läuft).	
				KernelSingletonTHM objKernelSingleton = KernelSingletonTHM.getInstance();
				IHibernateContextProviderZZZ objHibernateContext = HibernateUtilTHM.getHibernateContextProviderUsed(objKernelSingleton);
				objHibernateContext.getConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");  //! Jetzt erst wird jede Tabelle über den Anwendungsstart hinaus gespeichert UND auch wiedergeholt.				
				
				//### Hole das Troop-Objekt hier. 
				TroopDao daoTroop = new TroopDao(objHibernateContext);
				Troop objTroop = (Troop) daoTroop.searchTroopByUniquename(sUniqueName);
				if(objTroop == null){					
					sReturn = "KEIN Troop-Objekt mit dem UniqueName '" + sUniqueName + "' gefunden.";
					System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sReturn);
					break main;
				}			
				sReturn = "Troop-Objekt mit dem UniqueName '" + sUniqueName + "' gefunden.";
				System.out.println("###############################################################################");
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sReturn);
				System.out.println("###############################################################################");
				
				String sTroopType = objTroop.getTroopType();				
				sReturn = "Troop-Objekt mit dem UniqueName '" + sUniqueName + "' hat den TroopType='"+ sTroopType +"'.";
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sReturn);
				
				TileDaoFacadeFactoryTHM objDaoFacadeFactory = TileDaoFacadeFactoryTHM.getInstance(objKernelSingleton);
				TileDaoFacade objFacade = (TileDaoFacade) objDaoFacadeFactory.createDaoFacade(objTroop);
				sReturn = "TileDaoFacade-Objekt für JNDI erstellt.";
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sReturn);
				
				boolean bSuccess = objFacade.delete(objTroop);
				if(bSuccess){
					sReturn = "Erfolgreich gelöscht";
				}else{
					sReturn = "NICHT erfolgreich gelöscht.";					
				}			
				System.out.println("###############################################################################");
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sReturn);
				System.out.println("###############################################################################");
				
				//Nach dem Update soll mit dem UI weitergearbeitet werden können			
				objHibernateContext.closeAll();
				System.out.println("SessionFactory über den HibernateContextProvider geschlossen.... Nun wieder bearbeitbar im Java Swing Client?");				
			}//end main:
		} catch (ExceptionZZZ e) {
			System.out.println(e.getDetailAllLast());
			e.printStackTrace();		
		}
		return sReturn;
	}
	
	public String fillMapCreateNewTile(String sTroopType, long lngTroopVariant_Thiskeyid, String sMapAlias, String sX, String sY ){
		String sReturn = null;
		try{
			main:{				
				if(StringZZZ.isEmpty(sTroopType)) break main;
				if(StringZZZ.isEmpty(sMapAlias)) break main;
				if(StringZZZ.isEmpty(sX)) break main;
				if(StringZZZ.isEmpty(sY)) break main;
				
				//HOLE DIE SESSIONFACTORY PER JNDI:
				//Merke: DAS FUNKTIONIERT NUR, WENN DIE ANWENDUNG IN EINEM SERVER (z.B. Tomcat läuft).
				KernelSingletonTHM objKernelSingleton = KernelSingletonTHM.getInstance();	
				IHibernateContextProviderZZZ objHibernateContext = HibernateUtilTHM.getHibernateContextProviderUsed(objKernelSingleton);
				objHibernateContext.getConfiguration().setProperty("hibernate.hbm2ddl.auto", "update");  //! Jetzt erst wird jede Tabelle über den Anwendungsstart hinaus gespeichert UND auch wiedergeholt.				
				
				//### Hole das Varianten-Objekt hier. Mit einer Factory.				
				TroopVariantDaoFactory objVariantDaoFactory = TroopVariantDaoFactory.getInstance(objKernelSingleton);
				//TroopVariantDao daoVariant = (TroopVariantDao) objVariantDaoFactory.createDaoVariantJndi(lngTroopVariant_Thiskeyid);
				TroopVariantDao daoVariant = (TroopVariantDao) objVariantDaoFactory.createDaoVariant(lngTroopVariant_Thiskeyid);
				sReturn = "TroopVariantDao-Objekt für JNDI erstellt.";
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sReturn);
				
				TroopVariant objTroopVariant = (TroopVariant) daoVariant.searchKey(lngTroopVariant_Thiskeyid );
				if(objTroopVariant == null){
						
					sReturn = "KEIN TroopVariant-Objekt mit dem ThisKey '" + lngTroopVariant_Thiskeyid + "' gefunden.";
					System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sReturn);
					break main;
				}			
				sReturn = "TroopVariant-Objekt mit dem ThisKey '" + lngTroopVariant_Thiskeyid + "' gefunden.";
				System.out.println("###############################################################################");
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sReturn);
				System.out.println("###############################################################################");
					
				
				//Hole die Spielfeldzelle (Merke: Beim Erstellen mehrerer Spielsteine ist es performanter alle Zellen zu holen und in einer Schleife durchzugehen, als gezielt immer eine Zelle zu suchen)
				CellId primaryKey = new CellId(sMapAlias, sX, sY);//Die vorhandenen Schlüssel Klasse
					
				AreaCellDao daoAreaCell = new AreaCellDao(objHibernateContext);	
				AreaCell objCell = daoAreaCell.findByKey(primaryKey);			
				if(objCell==null){
					sReturn = "Zelle mit x/Y in Tabelle '" + sMapAlias + "' NICHT gefunden (" + sX + "/" + sY + ")";
					System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sReturn);
					break main;
				}else{
					sReturn = "Zelle mit x/Y in Tabelle '" + sMapAlias + "' gefunden (" + sX + "/" + sY + ")";
					System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sReturn);						
				}
				//daoAreaCell.getSession().clear();
				//daoAreaCell.getSession().close();
									
				TileDaoFacadeFactoryTHM objDaoFacadeFactory = TileDaoFacadeFactoryTHM.getInstance(objKernelSingleton);
				//TileDaoFacade objFacade = (TileDaoFacade) objDaoFacadeFactory.createDaoFacadeJndi(sTroopType);
				TileDaoFacade objFacade = (TileDaoFacade) objDaoFacadeFactory.createDaoFacade(sTroopType);
				sReturn = "TileDaoFacade-Objekt für JNDI erstellt.";
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sReturn);
				
				String sUniquename = objFacade.insert(objTroopVariant, objCell);
				if(!StringZZZ.isEmpty(sUniquename)){
					sReturn = "Erfolgreich eingefügt als '" + sUniquename + "'";					
				}else{
					//Nimm auch den Grund entgegen, warum das Einfügen gescheitert ist, z.B.:
					//a) Zelle voll
					//b) Gelände ist nicht erlaubt für diesen Einheitentyp
					sReturn = "NICHT erfolgreich eingefügt.";	
					
					//Nimm die FacadeResults entgegen. Sie enthalten die Fehlermeldung.
					String sFacadeResult = objFacade.getFacadeResult().getMessage();					
					sReturn = sReturn + "(" + sFacadeResult + ")";					
				}			
				System.out.println("###############################################################################");
				System.out.println(ReflectCodeZZZ.getPositionCurrent() + ": " + sReturn);
				System.out.println("###############################################################################");
				
				//Nach dem Update soll mit dem UI weitergearbeitet werden können			
				objHibernateContext.closeAll();
				System.out.println("SessionFactory über den HibernateContextProvider geschlossen.... Nun wieder bearbeitbar im Java Swing Client?");				
			}//end main:
		} catch (ExceptionZZZ e) {
			System.out.println(e.getDetailAllLast());
			e.printStackTrace();		
		}
		return sReturn;
	}
	
}
