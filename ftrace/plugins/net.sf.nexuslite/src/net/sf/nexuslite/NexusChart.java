package net.sf.nexuslite;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import net.sf.aftrace.chart.model.TraceChart;
import net.tourbook.chart.ChartDataModel;
import net.tourbook.chart.ChartDataXSerie;
import net.tourbook.chart.ChartDataYSerie;
import net.tourbook.chart.ChartType;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.PageBook;

public class NexusChart {

	@Inject private IEclipseContext eclipseContext;
	
	/* UI Controls */
	private PageBook				_pageBook;
	private Label					_pageNoChart;
	
	private TraceChart				_tourChart;
	
	/* TMF Events */
	private ITmfTrace currentTrace;
    
    protected TmfTraceManager fTraceManager;
    
	@PostConstruct
	public void createPartControl(final Composite parent) throws ExecutionException{
		
		/* regist event manager */
		fTraceManager = TmfTraceManager.getInstance();
	    TmfSignalManager.register(this);
	    
		createUI(parent);
		
		final ChartDataModel chartDataModel = new ChartDataModel(ChartType.LINE);
		
		{
			int count = 1000;
			Random rnd = new Random();
			
			final double[] distanceSerie = new double[count];
			for(int i=0; i<count; i++) distanceSerie[i] = i * 10;
			
			ChartDataXSerie xDataDistance = new ChartDataXSerie(distanceSerie);
			xDataDistance.setLabel("distance");
			xDataDistance.setUnitLabel("m");
			xDataDistance.setValueDivisor(1000);
			xDataDistance.setDefaultRGB(new RGB(0, 0, 0));
			
			chartDataModel.setXData2nd(xDataDistance);
			chartDataModel.addXyData(xDataDistance);
			
			final double[] timeSerie = new double[count];	
			for(int i=0; i<count; i++) timeSerie[i] = i;
			
			final ChartDataXSerie xDataTime = new ChartDataXSerie(timeSerie);
			
			xDataTime.setLabel("time");
			xDataTime.setUnitLabel("second");
			xDataTime.setDefaultRGB(new RGB(0, 0, 0));
			xDataTime.setAxisUnit(ChartDataXSerie.AXIS_UNIT_NUMBER);
			
			chartDataModel.setXData(xDataTime);
			chartDataModel.addXyData(xDataTime);
		}
		
		ChartDataYSerie y0_DataSpeed = genRndYSeries("Y_0");
		chartDataModel.addXyData(y0_DataSpeed);
		chartDataModel.addYData(y0_DataSpeed);
		
		ChartDataYSerie y1_DataSpeed = genRndYSeries("Y_1");
		chartDataModel.addXyData(y1_DataSpeed);
		chartDataModel.addYData(y1_DataSpeed);
		
		_tourChart.updateChart(chartDataModel, true);
		
		_pageBook.showPage(_pageNoChart);
		
		Display.getCurrent().asyncExec(new Runnable() {

			@Override
			public void run() {
				_pageBook.showPage(_tourChart);
				
			}
			
		});
	}
	
	private void createUI(final Composite parent) {

		_pageBook = new PageBook(parent, SWT.NONE);

		_pageNoChart = new Label(_pageBook, SWT.NONE);
		_pageNoChart.setText("TraceChart");

		_tourChart = new TraceChart(_pageBook, SWT.FLAT);
		_tourChart.setShowZoomActions(true);
		_tourChart.setShowSlider(true);
		
		/* Fix me */
		//_tourChart.setToolBarManager(getViewSite().getActionBars().getToolBarManager(), true);
		//_tourChart.setContextProvider(new TourChartContextProvider(this));

	}
	
	private ChartDataYSerie genRndYSeries(String yName){
		
		int count = 1000;
		Random rnd = new Random();
		
		float[] speedSerie = new float[count];
		for(int i=0; i<count; i++) speedSerie[i] = rnd.nextInt(10) * 10;
		
		ChartDataYSerie yDataSpeed = new ChartDataYSerie(ChartType.BAR, speedSerie);
		yDataSpeed.setYTitle(yName);
		yDataSpeed.setUnitLabel(yName);
		yDataSpeed.setShowYSlider(true);
		
		return yDataSpeed;
	}
	
	private ChartDataModel genChartDataModel(final double x[], final float y[]){
		
		final ChartDataModel chartDataModel = new ChartDataModel(ChartType.LINE);
		
		int count = x.length;
		final double[] distanceSerie = new double[count];
		for(int i=0; i<count; i++) distanceSerie[i] = i * 10;
		
		ChartDataXSerie xDataDistance = new ChartDataXSerie(distanceSerie);
		xDataDistance.setLabel("distance");
		xDataDistance.setUnitLabel("m");
		xDataDistance.setValueDivisor(1000);
		xDataDistance.setDefaultRGB(new RGB(0, 0, 0));
		
		chartDataModel.setXData2nd(xDataDistance);
		chartDataModel.addXyData(xDataDistance);
		
		final ChartDataXSerie xDataTime = new ChartDataXSerie(x);
		
		xDataTime.setLabel("time");
		xDataTime.setUnitLabel("second");
		xDataTime.setDefaultRGB(new RGB(0, 0, 0));
		xDataTime.setAxisUnit(ChartDataXSerie.AXIS_UNIT_NUMBER);
		
		chartDataModel.setXData(xDataTime);
		chartDataModel.addXyData(xDataTime);
		
		ChartDataYSerie y0_DataSpeed = new ChartDataYSerie(ChartType.BAR, y);
		y0_DataSpeed.setYTitle("Y_0");
		y0_DataSpeed.setUnitLabel("Y_0");
		y0_DataSpeed.setShowYSlider(true);
		
		chartDataModel.addXyData(y0_DataSpeed);
		chartDataModel.addYData(y0_DataSpeed);
		
		ChartDataYSerie y1_DataSpeed = new ChartDataYSerie(ChartType.BAR, y);
		y1_DataSpeed.setYTitle("Y_1");
		y1_DataSpeed.setUnitLabel("Y_1");
		y1_DataSpeed.setShowYSlider(true);
		
		chartDataModel.addXyData(y1_DataSpeed);
		chartDataModel.addYData(y1_DataSpeed);
		
		return chartDataModel;
		
	}
	
	@TmfSignalHandler
	public void traceSelected(final TmfTraceSelectedSignal signal) {

		System.out.println("NexusChart traceSelected "+ signal);
		
		// Don't populate the view again if we're already showing this trace
		if (currentTrace == signal.getTrace()) {
			return;
		}
		
		currentTrace = signal.getTrace();

		// Create the request to get data from the trace
		 TmfEventRequest req = new TmfEventRequest(TmfEvent.class,
	                TmfTimeRange.ETERNITY, TmfEventRequest.ALL_DATA,
	                ExecutionType.BACKGROUND) {

	            ArrayList<Double> xValues = new ArrayList<Double>();
	            ArrayList<Double> yValues = new ArrayList<Double>();

	            @Override
	            public void handleData(ITmfEvent data) {
	                // Called for each event
	                super.handleData(data);
	                ITmfEventField field = data.getContent().getField("value");
	                if (field != null) {
	                	int value = (Integer) field.getValue();
	                	yValues.add((double) value);
	                    xValues.add((double) data.getTimestamp().getValue());
	                }
	            }

	            @Override
				public void handleSuccess() {
					// Request successful, not more data available
					super.handleSuccess();

					final double x[] = toArray(xValues);
					final double y[] = toArray(yValues);

					System.out.println("x[] "+ x.length +" "+ x[0] +" "+ x[1] +" "+ x[2]);
					System.out.println("y[] "+ y.length +" "+ y[0] +" "+ y[1] +" "+ y[2]);
				}
	            
	            /* For update Chart
	             * 
	            @Override
	            public void handleSuccess() {
	                // Request successful, not more data available
	                super.handleSuccess();

	                final double x[] = toArray(xValues);
	                final double y[] = toArray(yValues);
	                final float  y1[] = tofArray(yValues);

	                final ChartDataModel chartDataModel = genChartDataModel(x, y1);
	                
	                // This part needs to run on the UI thread since it updates the chart SWT control
	                Display.getDefault().asyncExec(new Runnable() {

	                    @Override
	                    public void run() {
	                       
	                    	_tourChart.updateChart(chartDataModel, true);
	                    	
	                    	_tourChart.redraw();
	                    	
	                    	System.out.println("NexusChart handleSuccess");
	                    	
	                    }

	                });
	            }
				*/
	            
	            /**
	             * Convert List<Double> to double[]
	             */
	            private double[] toArray(List<Double> list) {
	                double[] d = new double[list.size()];
	                for (int i = 0; i < list.size(); ++i) {
	                    d[i] = list.get(i);
	                }

	                return d;
	            }
	            
	            private float[] tofArray(List<Double> list) {
	            	float[] d = new float[list.size()];
	                for (int i = 0; i < list.size(); ++i) {
	                	
	                	d[i] = list.get(i).floatValue();
	                }

	                return d;
	            }
	            
	        };
		
		ITmfTrace trace = signal.getTrace();
		trace.sendRequest(req);
	}
}
