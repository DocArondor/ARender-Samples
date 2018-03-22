package com.arondor;

import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Factory;
import com.filenet.api.core.IndependentlyPersistableObject;
import com.filenet.api.engine.EventActionHandler;
import com.filenet.api.events.ObjectChangeEvent;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.util.Id;

public class EventActionArenderRendition implements EventActionHandler
{
	@Override
	public void onEvent(ObjectChangeEvent event, Id id) throws EngineRuntimeException 
	{
		event.get_SourceObjectId();
		IndependentlyPersistableObject queueEntry = Factory.CmAbstractQueueEntry.createInstance(event.getObjectStore(), "arenderqueuesweep");
		queueEntry.getProperties().putValue("idDocument", event.get_SourceObjectId());
		queueEntry.save(RefreshMode.NO_REFRESH);
	}
}