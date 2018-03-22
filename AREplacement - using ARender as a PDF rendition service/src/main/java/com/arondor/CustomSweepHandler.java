package com.arondor;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.constants.ReservationType;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.IndependentlyPersistableObject;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.engine.HandlerCallContext;
import com.filenet.api.engine.SweepActionHandler;
import com.filenet.api.engine.SweepItemOutcome;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.exception.ExceptionCode;
import com.filenet.api.sweep.CmSweep;
import com.filenet.api.sweep.CmSweepPolicy;
import com.filenet.api.util.Id;

public class CustomSweepHandler implements SweepActionHandler
{
	private static final String ARENDER_URL = "http://yourARenderURL";

	// Implement for custom job and queue sweeps.
	@Override
	public void onSweep(CmSweep sweepObject, SweepActionHandler.SweepItem[] sweepItems) throws EngineRuntimeException
	{
		HandlerCallContext hcc = HandlerCallContext.getInstance();
		hcc.traceDetail("Entering CustomSweepHandler.onSweep");

		// Retrieve basic filenet objects
		ObjectStore os = sweepObject.getObjectStore();

		// Iterate the sweepItems and change the class.
		for (int i = 0; i < sweepItems.length; i++)
		{
			// At the top of your loop, always check to make sure
			// that the server is not shutting down.
			// If it is, clean up and return control to the server.

			if (hcc != null && hcc.isShuttingDown())
			{
				throw new EngineRuntimeException(ExceptionCode.E_BACKGROUND_TASK_TERMINATED,
						this.getClass().getSimpleName()
						+ " is terminating prematurely because the server is shutting down");
			}

			// Extract the target object from the SweepItem array.
			IndependentlyPersistableObject obj = sweepItems[i].getTarget();
			String msg = "sweepItems[" + i + "]= " + obj.getProperties().getIdValue("idDocument");
			Id idDoc = obj.getProperties().getIdValue("idDocument");

			hcc.traceDetail(msg);
			try
			{
				Document doc = Factory.Document.fetchInstance(os, idDoc, null);
				ContentElementList docContentList = doc.get_ContentElements();
				Iterator iter = docContentList.iterator();
				InputStream stream = null;
				ContentElementList contentList = Factory.ContentTransfer.createList();
				while (iter.hasNext() )
				{
					ContentTransfer ct = (ContentTransfer) iter.next();
					stream = ct.accessContentStream();


					ContentTransfer ctObject = Factory.ContentTransfer.createInstance();

					
					ctObject.setCaptureSource(getARenderInputStream(stream));
					ctObject.set_ContentType("application/pdf");
					ctObject.set_RetrievalName(doc.get_Name() + ".pdf");
					contentList.add(ctObject);
				}
				doc.checkout(ReservationType.EXCLUSIVE, null, doc.getClassName(), doc.getProperties());
				doc.save(RefreshMode.REFRESH);
				Document reservation = (Document) doc.get_Reservation();
				reservation.set_ContentElements(contentList);
				reservation.save(RefreshMode.REFRESH);
				reservation.checkin(AutoClassify.AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
				reservation.save(RefreshMode.NO_REFRESH);

				// Set outcome to PROCESSED if item processed successfully.
				sweepItems[i].setOutcome(SweepItemOutcome.PROCESSED,
						"item processed by " + this.getClass().getSimpleName());
			}
			// Set failure status on objects that fail to process.
			catch (EngineRuntimeException e)
			{
				sweepItems[i].setOutcome(SweepItemOutcome.FAILED, "CustomSweepHandler: " + e.getMessage());
			}
			catch (IOException e)
			{
				sweepItems[i].setOutcome(SweepItemOutcome.FAILED, "CustomSweepHandler: " + e.getMessage());
			}
		}
		hcc.traceDetail("Exiting CustomSweepHandler.onSweep");
		System.out.println("Exiting CustomSweepHandler.onSweep");
	}

	private InputStream getARenderInputStream(InputStream stream) throws IOException
	{
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(ARENDER_URL + "arendergwt/uploadServlet");

		final File tmpFile = new File(System.getProperty("java.io.tmprdir") + UUID.randomUUID().toString());
		FileOutputStream fos = new FileOutputStream(tmpFile);
		IOUtils.copy(stream, fos);

		FileBody uploadFilePart = new FileBody(tmpFile);
		MultipartEntity reqEntity = new MultipartEntity();
		reqEntity.addPart("upload-file", uploadFilePart);
		httpPost.setEntity(reqEntity);

		HttpResponse response = httpclient.execute(httpPost);

		String uuid = new String(this.read(response.getEntity().getContent())).replace("|", "").trim();

		tmpFile.delete();

		URL url = new URL(ARENDER_URL + "arendergwt/downloadServlet?uuid=" + uuid + "&type=RENDERED");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		return con.getInputStream();
	}

	protected void sendData(HttpURLConnection con, InputStream data) throws IOException
	{
		DataOutputStream wr = null;
		try
		{
			wr = new DataOutputStream(con.getOutputStream());
			IOUtils.copy(data, wr);
			wr.flush();
			wr.close();
		}
		catch (IOException exception)
		{
			throw exception;
		}
		finally
		{
			this.closeQuietly(wr);
		}
	}

	private byte[] read(InputStream is) throws IOException
	{
		try
		{
			return IOUtils.toByteArray(is);
		}
		catch (IOException ioe)
		{
			throw ioe;
		}
		finally
		{
			this.closeQuietly(is);
		}
	}

	protected void closeQuietly(Closeable closeable)
	{
		try
		{
			if (closeable != null)
			{
				closeable.close();
			}
		}
		catch (IOException ex)
		{

		}
	}

	/*
	 * Called automatically when the handler is invoked by a custom sweep job or
	 * sweep policy. Specify properties required by the handler, if any. If you
	 * return an empty array, then all properties are fetched.
	 */
	@Override
	public String[] getRequiredProperties()
	{
		String[] names = { PropertyNames.ID };
		return names;
	}

	/*
	 * Implement for custom sweep policies. This method is not implemented
	 * because this is an example of a custom sweep job.
	 */
	@Override
	public void onPolicySweep(CmSweep sweepObject, CmSweepPolicy policyObject,
			SweepActionHandler.SweepItem[] sweepItems)
	{
	}
}
