package net.sf.aftrace.chart.control;

import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import net.sf.aftrace.chart.model.TraceChart;
import net.tourbook.chart.ChartDataModel;
import net.tourbook.chart.ChartDataXSerie;
import net.tourbook.chart.ChartDataYSerie;
import net.tourbook.chart.ChartType;

public class ChartControlFactory {

	public static TraceChart createChartControl(Composite parent){
		
		TraceChart _tourChart = new TraceChart(parent, SWT.FLAT);
		_tourChart.setShowZoomActions(true);
		_tourChart.setShowSlider(true);;
		
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
		
//		ChartDataYSerie y1_DataSpeed = genRndYSeries("Y_1");
//		chartDataModel.addXyData(y1_DataSpeed);
//		chartDataModel.addYData(y1_DataSpeed);
		
		_tourChart.updateChart(chartDataModel, true);
		
		return _tourChart;
		
	}
	
	private static ChartDataYSerie genRndYSeries(String yName){
		
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
}
