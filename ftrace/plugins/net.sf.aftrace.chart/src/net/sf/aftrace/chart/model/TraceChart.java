package net.sf.aftrace.chart.model;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import net.sf.aftrace.chart.listener.ITraceChartSelectionListener;
import net.tourbook.chart.Chart;
import net.tourbook.chart.ChartDataModel;
import net.tourbook.chart.IHoveredListener;
import net.tourbook.common.PointLong;

public class TraceChart extends Chart {

	private static final String		ID										= "net.sf.e4ftrace.chart.model.TraceChart";
	
	private final ListenerList		_tourChartListeners						= new ListenerList();
	private final ListenerList		_xAxisSelectionListener					= new ListenerList();
	
	private ControlListener			_ttControlListener						= new ControlListener();
	
	private class ControlListener implements Listener {

		TraceChart	__traceChart = TraceChart.this;

		public void handleEvent(final Event event) {

			if (__traceChart.isDisposed()) return;

			if (event.widget instanceof Control) {

				switch (event.type) {
					case SWT.MouseEnter:
						//System.out.println("TraceChart : ControlListener : MouseEnter");
						break;
	
					case SWT.MouseExit:
						//System.out.println("TraceChart : ControlListener : MouseExit");
						break;
				}
			}
		}
	}
	
	public class HoveredListener implements IHoveredListener {

		@Override
		public void hideTooltip() {

			//System.out.println("TraceChart : HoveredListener : hideTooltip");
		}

		@Override
		public void hoveredValue(	final long eventTime,
									final int hoveredValueIndex,
									final PointLong devHoveredValue,
									final int devXMouseMove,
									final int devYMouseMove) {

			//System.out.println("TraceChart : HoveredListener : hoveredValue");
		}

	}
	
	public TraceChart(Composite parent, int style) {
		
		super(parent, style);
		
		/*
		 * when the focus is changed, fire a tour chart selection, this is neccesarry to update the
		 * tour markers when a tour chart got the focus
		 */
		addFocusListener(new Listener() {
			public void handleEvent(final Event event) {
				fireTraceChartSelection();
			}
		});
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(final DisposeEvent e) {
				onDispose();
			}
		});
		
		addControlListener(this);
		
		setShowMouseMode();
		
		setHoveredListener(new HoveredListener());
	}

	/**
	 * fire a selection event for this tour chart
	 */
	private void fireTraceChartSelection() {
		
		final Object[] listeners = _tourChartListeners.getListeners();
		
		for (final Object listener2 : listeners) {
			
			final ITraceChartSelectionListener listener = (ITraceChartSelectionListener) listener2;
			
			SafeRunnable.run(new SafeRunnable() {
			
				public void run() {
				
					listener.selectedTourChart(TraceChart.this);
				}
			});
		}
	}
	
	private void onDispose() {

		
	}
	
	/**
	 * ########################### Recursive #########################################<br>
	 * <p>
	 * Add listener to all controls within the tour chart
	 * <p>
	 * ########################### Recursive #########################################<br>
	 * 
	 * @param control
	 */
	private void addControlListener(final Control control) {

		control.addListener(SWT.MouseExit, _ttControlListener);
		control.addListener(SWT.MouseEnter, _ttControlListener);

		if (control instanceof Composite) {
			final Control[] children = ((Composite) control).getChildren();
			for (final Control child : children) {
				addControlListener(child);
			}
		}
	}
	
	public void setDataModel(ChartDataModel chartDataModel){
		
		super.setDataModel(chartDataModel);
		
	}
}
