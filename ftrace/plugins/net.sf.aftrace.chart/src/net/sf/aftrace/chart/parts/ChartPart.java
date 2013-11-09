package net.sf.aftrace.chart.parts;

import java.net.URI;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.sf.aftrace.chart.model.TraceChart;
import net.tourbook.chart.ChartDataModel;
import net.tourbook.chart.ChartDataXSerie;
import net.tourbook.chart.ChartDataYSerie;
import net.tourbook.chart.ChartType;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.PageBook;
import org.osgi.service.event.Event;


@SuppressWarnings("restriction")
public class ChartPart {

	@Inject private IEclipseContext eclipseContext;
	
	/* UI Controls */
	private PageBook				_pageBook;
	private Label					_pageNoChart;
	
	private TraceChart				_tourChart;
	
	@PostConstruct
	public void createPartControl(final Composite parent) throws ExecutionException{
		
		long timeStart = System.currentTimeMillis();
		long delta = System.currentTimeMillis() - timeStart;
		System.out.println("ChartPart : delta : "+ delta +", data :");
		
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
	
	@Inject
	@Optional
	public void partActivation(@UIEventTopic(UIEvents.UILifeCycle.ACTIVATE) Event event, MApplication application) {

		// Don't inject MPart and IEclipseContext! Need from root context here
		MPart activePart = (MPart) event.getProperty(UIEvents.EventTags.ELEMENT);
		
		IEclipseContext context = application.getContext();
		
		if (activePart != null) {
			
			context.set("myactivePartId", activePart.getElementId());
			
			System.out.println("ChartPart : activePart : "+ activePart.getElementId());
			
		}else{
			
			System.out.println("activePart : null");
			
		}
	}
}
