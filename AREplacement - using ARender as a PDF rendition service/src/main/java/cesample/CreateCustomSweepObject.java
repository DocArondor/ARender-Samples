package cesample;



import javax.crypto.Cipher;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import cesample.ConfigInfo;

import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.EngineCollection;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.AutoUniqueName;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.ComponentRelationshipType;
import com.filenet.api.constants.CompoundDocumentState;
import com.filenet.api.constants.DefineSecurityParentage;
import com.filenet.api.constants.GuidConstants;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.constants.SweepMode;
import com.filenet.api.constants.VersionBindType;
import com.filenet.api.core.ComponentRelationship;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Containable;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.CustomObject;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;

import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.ReferentialContainmentRelationship;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.security.AccessPermission;
import com.filenet.api.sweep.CmCustomQueueSweep;
import com.filenet.api.sweep.CmCustomSweepJob;
import com.filenet.api.sweep.CmSweepAction;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;


public class CreateCustomSweepObject {

	private static final String CE_URI = "yourContentEngineURI"; 

	private static final String OBJECT_STORE_NAME = "yourObjectStoreName";

	private static final String CE_USERID = "aValidUserId";
	private static final String CE_PASSWORD = "theValidPassword";
	private static final String JAAS_STANZA_NAME = "theJAASStanzaName";

	private static Connection conn = Factory.Connection.getConnection(CE_URI);

	private static CreateCustomSweepObject tb = new CreateCustomSweepObject();

	public static void main(String[] args) throws LoginException 
	{
		System.out.println("CE is at " + CE_URI);
		System.out.println("ObjectStore is " + OBJECT_STORE_NAME);
		
		// This is the standard Subject push/pop model for the helper methods.
		Subject subject = UserContext.createSubject(conn, CE_USERID, CE_PASSWORD,
			 JAAS_STANZA_NAME);
		UserContext.get().pushSubject(subject);
		try
		{
			tb.run(conn);
		}
		finally
		{
			UserContext.get().popSubject();
		}
	}

	/**
	 * This method contains the actual business logic.  Authentication has
	 * already happened by the time we get here.
	 */
	public void run(Connection conn)
	{
		// Standard Connection -> Domain -> ObjectStore
		//no R/T
		Domain dom = Factory.Domain.getInstance(conn, null);
		//no R/T
		ObjectStore os = Factory.ObjectStore.getInstance(dom, 
				OBJECT_STORE_NAME);

		// Creates a custom sweep job.
		CmCustomQueueSweep customqJob = Factory.CmCustomQueueSweep.createInstance(os, "CmCustomQueueSweep");
		customqJob.set_DisplayName("Java Custom queue sweep Job: conversion with ARender");
		// here we refer the class used as a target for sweep
		customqJob.set_SweepTarget(Factory.DocumentClassDefinition.getInstance(os, new Id("{70003261-0000-C41E-A78D-949E91C55058}")));


		// Get CmSweepAction object that references custom handler. In this case, it is the ID of the ARender code module
		CmSweepAction sweepAction = Factory.CmSweepAction.fetchInstance(os, 
				new Id("{502E2F61-0000-C012-886B-51022BB57C14}"), null);
		customqJob.set_SweepAction(sweepAction);

		customqJob.save(RefreshMode.NO_REFRESH);
	}

}