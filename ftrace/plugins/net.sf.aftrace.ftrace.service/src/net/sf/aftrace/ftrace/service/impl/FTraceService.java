package net.sf.aftrace.ftrace.service.impl;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.events.IEventBroker;

import net.sf.aftrace.ftrace.service.IFTraceService;

@Singleton
@Creatable
public class FTraceService implements IFTraceService{

//	@Inject private IEventBroker eventBroker;
//	
//	@Inject private IEclipseContext eclipseContext;
	
	@PostConstruct
	public void postConstruct() {
		
		System.out.println("TraceService init");
		
	}
	
	public void print(){
		
		System.out.println("FTraceService print");
		
	}
}
