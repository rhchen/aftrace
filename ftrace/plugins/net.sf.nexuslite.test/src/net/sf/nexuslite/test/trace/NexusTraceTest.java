package net.sf.nexuslite.test.trace;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

import net.sf.nexuslite.NexusTrace;
import net.sf.nexuslite.test.Activator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.tmf.core.component.TmfProviderManager;
import org.eclipse.linuxtools.tmf.core.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NexusTraceTest {

	private static final long DEFAULT_INITIAL_OFFSET_VALUE = (1L * 100 * 1000 * 1000); // .1sec
	private static final int BLOCK_SIZE = 500;
	private static final int NB_EVENTS = 10000;
	private static NexusTrace fTrace = null;

	private static int SCALE = -3;

	@Before
	public void setUp() throws Exception {
		
		setupTrace("samples//nexusTrace");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testProcessEventRequestForNbEvents()
			throws InterruptedException {
		
		final int blockSize = 100;
		final int nbEvents = 1000;
		final Vector<ITmfEvent> requestedEvents = new Vector<ITmfEvent>();

		final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG,TmfTimestamp.BIG_CRUNCH);
		
		final TmfEventRequest request = new TmfEventRequest(ITmfEvent.class,range, nbEvents, blockSize) {
			
			@Override
			public void handleData(final ITmfEvent event) {
				super.handleData(event);
				requestedEvents.add(event);
			}
		};
		
		final ITmfDataProvider[] providers = TmfProviderManager.getProviders(ITmfEvent.class, NexusTrace.class);
		providers[0].sendRequest(request);
		request.waitForCompletion();

		assertEquals("nbEvents", nbEvents, requestedEvents.size());
		assertTrue("isCompleted", request.isCompleted());
		assertFalse("isCancelled", request.isCancelled());

		// Ensure that we have distinct events.
		// Don't go overboard: we are not validating the stub!
		for (int i = 0; i < nbEvents; i++) {
			assertNotEquals("Distinct events", i + 1, requestedEvents.get(i)
					.getTimestamp().getValue());
		}
	}

	@Test
	public void testProcessEventRequestForSomeEvents()
			throws InterruptedException {
		
		final int blockSize = 1;
		final long startTime = 100;
		final int nbEvents = 1000;
		final Vector<ITmfEvent> requestedEvents = new Vector<ITmfEvent>();

		final TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(startTime,SCALE), TmfTimestamp.BIG_CRUNCH);
		
		final TmfEventRequest request = new TmfEventRequest(ITmfEvent.class,
				range, nbEvents, blockSize) {
			
			@Override
			public void handleData(final ITmfEvent event) {
				super.handleData(event);
				requestedEvents.add(event);
			}
		};
		
		final ITmfDataProvider[] providers = TmfProviderManager.getProviders(ITmfEvent.class, NexusTrace.class);
		
		providers[0].sendRequest(request);
		request.waitForCompletion();

		assertEquals("nbEvents", nbEvents, requestedEvents.size());
		assertTrue("isCompleted", request.isCompleted());
		assertFalse("isCancelled", request.isCancelled());

		// Ensure that we have distinct events.
		// Don't go overboard: we are not validating the stub!
		for (int i = 1; i < nbEvents; i++) {
			
			assertNotEquals("Distinct events", 
					requestedEvents.get(i-1).getTimestamp().getValue(),
					requestedEvents.get(i).getTimestamp().getValue());
		}
	}

	// @Test
	public void testProcessEventRequestForOtherEvents()
			throws InterruptedException {
		
		final int blockSize = 1;
		final int startIndex = 99;
		final long startTime = 100;
		final int nbEvents = 1000;
		final Vector<ITmfEvent> requestedEvents = new Vector<ITmfEvent>();

		final TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(startTime,SCALE), TmfTimestamp.BIG_CRUNCH);
		
		final TmfEventRequest request = new TmfEventRequest(ITmfEvent.class,range, startIndex, nbEvents, blockSize) {
			
			@Override
			public void handleData(final ITmfEvent event) {
				super.handleData(event);
				requestedEvents.add(event);
			}
		};
		
		final ITmfDataProvider[] providers = TmfProviderManager.getProviders(ITmfEvent.class, NexusTrace.class);
		providers[0].sendRequest(request);
		request.waitForCompletion();

		assertEquals("nbEvents", nbEvents, requestedEvents.size());
		assertTrue("isCompleted", request.isCompleted());
		assertFalse("isCancelled", request.isCancelled());

		// Ensure that we have distinct events.
		// Don't go overboard: we are not validating the stub!
		for (int i = 1; i < nbEvents; i++) {
			assertNotEquals("Distinct events", 
					requestedEvents.get(i-1).getTimestamp().getValue(),
					requestedEvents.get(i).getTimestamp().getValue());
		}
	}

	// @Test
	public void testProcessDataRequestForSomeEvents()
			throws InterruptedException {
		final int startIndex = 100;
		final int nbEvents = 1000;
		final Vector<ITmfEvent> requestedEvents = new Vector<ITmfEvent>();

		final TmfDataRequest request = new TmfDataRequest(ITmfEvent.class,startIndex, nbEvents) {
			
			@Override
			public void handleData(final ITmfEvent event) {
				super.handleData(event);
				requestedEvents.add(event);
			}
		};
		final ITmfDataProvider[] providers = TmfProviderManager.getProviders(ITmfEvent.class, NexusTrace.class);
		providers[0].sendRequest(request);
		request.waitForCompletion();

		assertEquals("nbEvents", nbEvents, requestedEvents.size());
		assertTrue("isCompleted", request.isCompleted());
		assertFalse("isCancelled", request.isCancelled());

		// Ensure that we have distinct events.
		// Don't go overboard: we are not validating the stub!
		for (int i = 1; i < nbEvents; i++) {
			assertNotEquals("Distinct events", 
					requestedEvents.get(i-1).getTimestamp().getValue(),
					requestedEvents.get(i).getTimestamp().getValue());
		}
	}
	
	@Test
	public void testProcessEventRequestForAllEvents()
			throws InterruptedException {
		
		final int blockSize = 1;
		
		final Vector<ITmfEvent> requestedEvents = new Vector<ITmfEvent>();

		final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
		
		final TmfEventRequest request = new TmfEventRequest(ITmfEvent.class,range, NB_EVENTS, blockSize) {
			
			@Override
			public void handleData(final ITmfEvent event) {
				super.handleData(event);
				requestedEvents.add(event);
				//System.out.println("event : "+ event.toString());
			}
		};
		
		final ITmfDataProvider[] providers = TmfProviderManager.getProviders(ITmfEvent.class, NexusTrace.class);
		
		providers[0].sendRequest(request);
		
		request.waitForCompletion();

		assertEquals("nbEvents", NB_EVENTS, requestedEvents.size());
		assertTrue("isCompleted", request.isCompleted());
		assertFalse("isCancelled", request.isCancelled());

		for (int i = 1; i < NB_EVENTS; i++) {
			
			 long timestamp = requestedEvents.get(i).getTimestamp().getValue();
			 ITmfEventField field = requestedEvents.get(i).getContent().getField("value");
			 int value = (Integer) field.getValue();
			 
			 //System.out.println("event : "+ timestamp +" "+ value);
			 
			 assertNotEquals("Distinct events", 
						requestedEvents.get(i-1).getTimestamp().getValue(),
						requestedEvents.get(i).getTimestamp().getValue());
		}
	}
	
	private static NexusTrace setupTrace(final String path) {
        
		if (fTrace == null) {
           
			try {
               
				final URL location = FileLocator.find(Activator.getDefault().getBundle(), new Path(path), null);
                
				final File test = new File(FileLocator.toFileURL(location).toURI());
                
				fTrace = new NexusTrace();
                
				fTrace.initTrace(null, test.toURI().getPath(), ITmfEvent.class);
				
				fTrace.indexTrace(true);
				
            } catch (final TmfTraceException e) {
                e.printStackTrace();
            } catch (final URISyntaxException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            } catch (final Exception e){
            	e.printStackTrace();
            }
        }
		
        return fTrace;
    }
}
