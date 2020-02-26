package Frontend;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class GUI {
	private final static Display display = new Display();
	private static Shell shell = new Shell(display);
	private static HashMap<LocalDate, Day> days = new HashMap<LocalDate, Day>();
	private static InitialValues initialValues = new InitialValues();

	public static void main(String[] args) {
		try {
			File file = new File("assets/initialValues.txt");
			if(!file.exists()) {
				file.createNewFile();
			}
			file = new File("assets/data.txt");
			if(!file.exists()) {
				file.createNewFile();
			}
			loadData(days, initialValues);
		} catch (IOException e) {
			
		}
		
		shell.setText("Hour Logger");
		shell.setLayout(new FillLayout());
		new MainScreen(shell, days, initialValues);

		Point size = shell.getSize();
		Rectangle screen = display.getMonitors()[0].getBounds();
		shell.setBounds(
				(screen.width-size.x)/2,
				(screen.height-size.y)/2,
				size.x,
				size.y
		);
		shell.open();
		shell.addListener(SWT.Close, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				System.exit(0);
			}
		});
		while (!display.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
	
	public static void loadData(HashMap<LocalDate, Day> days, InitialValues intialValues) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(
				"assets/data.txt"));
		
		String line = reader.readLine();
		while (line != null) {
			String[] data = line.split(",");
			//YYYY-MM-DD
			String[] dateDigits = data[0].split("-");
			LocalDate date = LocalDate.of(Integer.parseInt(dateDigits[0]),
										  Integer.parseInt(dateDigits[1]),
										  Integer.parseInt(dateDigits[2]));
			ArrayList<Interval> intervals = new ArrayList<Interval>();
			Interval interval = new Interval();
			String[] timeDigits = {};
			LocalTime time = null;
			for(int i = 1; i < data.length; i++) {
				switch (i % 3) {
					case 0:
						timeDigits = data[i].split(":");
						time = LocalTime.of(Integer.parseInt(timeDigits[0]), Integer.parseInt(timeDigits[1]));
						interval.setEndTime(time);
						intervals.add(interval);
						break;
					case 1:
						interval = new Interval();
						interval.setShift(data[i]);
						break;
					case 2:
						timeDigits = data[i].split(":");
						time = LocalTime.of(Integer.parseInt(timeDigits[0]), Integer.parseInt(timeDigits[1]));
						interval.setStartTime(time);
						break;
				}
			}
			days.put(date, new Day(date, intervals));
			// read next line
			line = reader.readLine();
		}
		
		reader.close();
		
		reader = new BufferedReader(new FileReader(
				"assets/initialValues.txt"));
		line = reader.readLine();
		if(line != null) {
			String[] intialValueData = line.split(",");
			intialValues.setWage(Double.parseDouble(intialValueData[0]));
		}
		
		reader.close();
	}
}

interface CurrentScreen {
    void changeScreen(Screens name, Shell Shell, HashMap<LocalDate, Day> data, InitialValues initialValues);
}