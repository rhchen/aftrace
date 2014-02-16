package net.sf.aftrace.ftrace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectorFactory;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.WorkbenchLogger;
import org.eclipse.e4.ui.services.internal.events.EventBroker;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfLongLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.osgi.framework.debug.FrameworkDebugOptions;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkUtil;

import net.sf.aftrace.ftrace.service.IFTraceService;
import net.sf.aftrace.ftrace.service.impl.FTraceService;;

public class TmfFTrace extends TmfTrace implements ITmfEventParser {

	public static final String PLUGIN_ID = "net.sf.aftrace.ftrace";
	
	private static final int CHUNK_SIZE = 65536; // seems fast on MY system
    private static final int EVENT_SIZE = 8; // according to spec

    private TmfLongLocation fCurrentLocation;
    private static final TmfLongLocation NULLLOCATION = new TmfLongLocation(
            (Long) null);
    private static final TmfContext NULLCONTEXT = new TmfContext(NULLLOCATION,
            -1L);

    private long fSize;
    private long fOffset;
    private File fFile;
    private String[] fEventTypes;
    private FileChannel fFileChannel;
    private MappedByteBuffer fMappedByteBuffer;
    
    @Inject private IFTraceService traceService;
    
    @Inject private IEventBroker eventBroker;
	
	@Inject private IEclipseContext eclipseContext;
	
	@Inject private Logger logger;
	
	
	@SuppressWarnings("restriction")
	@Override
	@Inject
	public IStatus validate(IProject project, String path) {
		
		File f = new File(path);
        if (!f.exists()) {
            return new Status(IStatus.ERROR, PLUGIN_ID,
                    "File does not exist"); //$NON-NLS-1$
        }
        
        if (!f.isFile()) {
            return new Status(IStatus.ERROR, PLUGIN_ID, path
                    + " is not a file"); //$NON-NLS-1$
        }
        
        InjectorFactory.getDefault().addBinding(IEclipseContext.class).implementedBy(EclipseContext.class);
        
        InjectorFactory.getDefault().addBinding(IExtensionRegistry.class).implementedBy(ExtensionRegistry.class);
        
        InjectorFactory.getDefault().addBinding(IEventBroker.class).implementedBy(EventBroker.class);
        
        
//        InjectorFactory.getDefault().addBinding(Logger.class).implementedBy(WorkbenchLogger.class);
//        
//        InjectorFactory.getDefault().addBinding(DebugOptions.class).implementedBy(FrameworkDebugOptions.class);
        
        InjectorFactory.getDefault().addBinding(IFTraceService.class).implementedBy(FTraceService.class);
        
        traceService = InjectorFactory.getDefault().make(IFTraceService.class, null);
        
        traceService.print();
        
//        IEclipseContext rootContext = EclipseContextFactory.getServiceContext(FrameworkUtil.getBundle(getClass()).getBundleContext());
//        
//        logger = ContextInjectionFactory.make(Logger.class, rootContext);
//        
//        logger.debug("aaa");
        
        return Status.OK_STATUS;
	}

	@Override
	public ITmfLocation getCurrentLocation() {
		
		return fCurrentLocation;
	
	}

	@Override
    public void initTrace(IResource resource, String path,
            Class<? extends ITmfEvent> type) throws TmfTraceException {
        
		super.initTrace(resource, path, type);
        fFile = new File(path);
        fSize = fFile.length();
        
        if (fSize == 0) {
            throw new TmfTraceException("file is empty"); //$NON-NLS-1$
        }
        
        fEventTypes = new String[]{"FTrace"};
                                             // the 'spec'
        if (fEventTypes.length != 64) {
            throw new TmfTraceException(
                    "Trace header does not contain 64 event names"); //$NON-NLS-1$
        }
        
        if (getNbEvents() < 1) {
            throw new TmfTraceException("Trace does not have any events"); //$NON-NLS-1$
        }
        
        try {
        	
            fFileChannel = new FileInputStream(fFile).getChannel();
        
        } catch (FileNotFoundException e) {
            throw new TmfTraceException(e.getMessage());
        } catch (IOException e) {
            throw new TmfTraceException(e.getMessage());
        }
    }
	
	@Override
	public double getLocationRatio(ITmfLocation location) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ITmfContext seekEvent(ITmfLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITmfContext seekEvent(double ratio) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITmfEvent parseEvent(ITmfContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
