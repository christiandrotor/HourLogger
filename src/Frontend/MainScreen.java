package Frontend;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MainScreen implements CurrentScreen {
	private ArrayList<LocalDate> weeks = new ArrayList<LocalDate>();
	private LocalDate selectedDate;
	
	public MainScreen(Shell shell, HashMap<LocalDate, Day> days, InitialValues initialValues) {
		selectedDate = LocalDate.now();
		loadWeeks(days);
		Composite mainScreen = new Composite(shell, SWT.NONE);
	    mainScreen.setLayout(new GridLayout(3, false));
	    // initial value group for calculations
	    Group initialValuesGrp = new Group(mainScreen, SWT.NONE);
		GridData gridD = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gridD.horizontalSpan = 3;
		initialValuesGrp.setLayoutData(gridD);
		initialValuesGrp.setLayout(new GridLayout(2, false));
		initialValuesGrp.setText("Initial Values");
	    // wage label
	    Label wageLabel = new Label(initialValuesGrp, SWT.NONE);
		wageLabel.setText("What is your current hourly wage?");
		wageLabel.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));
		// wage text
		Text wageText = new Text(initialValuesGrp, SWT.NONE);
		gridD = new GridData(SWT.NONE, SWT.NONE, false, false);
		gridD.widthHint = 35;
		wageText.setLayoutData(gridD);
		wageText.setToolTipText("Just a number\nEx. .1, 1, 1.1\nNo special characters");
		wageText.setText(Double.toString(initialValues.getWage()));
		// save/set intialValues
		Button saveIV = new Button(initialValuesGrp, SWT.PUSH);
		saveIV.setText("Save/Set");
		saveIV.addListener(SWT.MouseDown, new Listener() {
			
			@Override
			public void handleEvent(Event e) {
				if (wageText.getText().toUpperCase().equals("THANKS")) {
					wageText.setText(Double.toString(initialValues.getWage()));
					Utilities.displayMessage(shell, "Thanks for the love and support Mom and Dad!", 5000);
				} else {
					// save initialValuesObject
					initialValues.setWage(Double.parseDouble(wageText.getText()));
					// save to file for accessibility next time program started
					try {
						saveInitialValues(wageText.getText());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					Utilities.displayMessage(shell, "Saved", 1000);
				}
			}
		});
		gridD = new GridData(SWT.NONE, SWT.NONE, false, false);
		gridD.horizontalSpan = 2;
		saveIV.setLayoutData(gridD);		
		// date group
	    Group dateValues = new Group(mainScreen, SWT.NONE);
		gridD = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gridD.horizontalSpan = 3;
		dateValues.setLayoutData(gridD);
		dateValues.setLayout(new GridLayout(3, false));
		dateValues.setText("Date Selection");
		// date label
	    Label dateLabel = new Label(dateValues, SWT.NONE);
		dateLabel.setText("Please Pick a Date");
		gridD = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		dateLabel.setLayoutData(gridD);
		//date picker
		CDateTime cdt = new CDateTime(dateValues, CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		cdt.setSelection(new Date());
		gridD = new GridData(SWT.FILL, SWT.NONE, true, false);
		gridD.widthHint = 200;
		cdt.setLayoutData(gridD);
		cdt.setToolTipText("The date chosen will load the whole week that the chosen day is within");
		cdt.addListener(SWT.Modify, new Listener() {
			
			@Override
			public void handleEvent(Event e) {
				selectedDate = cdt.getSelection().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			}
		});
		// search button
		Button search = new Button(dateValues, SWT.PUSH);
		search.setText("Search");
		search.addListener(SWT.MouseDown, new Listener() {
			
			@Override
			public void handleEvent(Event e) {
				mainScreen.dispose();
				changeScreen(Screens.WeekLogger, shell, days, initialValues);
			}
		});
		gridD = new GridData(SWT.NONE, SWT.NONE, false, false);
		search.setLayoutData(gridD);
		// check to display previous weeks
		if (weeks.size() != 0) {
			// OR label
		    CLabel or = new CLabel(dateValues, SWT.NONE);
		    or.setTopMargin(25);
			or.setText("OR");
			gridD = new GridData(SWT.CENTER, GridData.CENTER, false, false);
			gridD.horizontalSpan = 3;
			gridD.heightHint = 50;
			or.setLayoutData(gridD);
			// previous weeks label
		    Label prevWeekLabel = new Label(dateValues, SWT.NONE);
			prevWeekLabel.setText("Previous Entered Weeks");
			gridD = new GridData(SWT.NONE, SWT.NONE, false, false);
			gridD.horizontalSpan = 3;
			prevWeekLabel.setLayoutData(gridD);
			// Previous week records
			List dates = new List(dateValues, SWT.NONE);
			weeks.forEach(week -> {
				dates.add(week.toString());
			});
			dates.addListener(SWT.MouseDown, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					selectedDate = weeks.get(dates.getSelectionIndex());
					mainScreen.dispose();
					changeScreen(Screens.WeekLogger, shell, days, initialValues);
				}		
			});
			gridD = new GridData(SWT.NONE, SWT.NONE, false, false);
			gridD.horizontalSpan = 3;
			dates.setLayoutData(gridD);
		}
		// set the window size
		Point windowSize = mainScreen.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		shell.setSize(500, windowSize.y + 50);
		shell.layout();	
	}
	
	private void saveInitialValues(String wage) throws IOException {
		File fout = new File("assets/initialValues.txt");
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		bw.write(wage + ",");
		bw.newLine();
		
		bw.close();
	}
	
	private void loadWeeks(HashMap<LocalDate, Day> days) {
		Day[] dayArray = days.values().toArray(new Day[0]);
	    for(int i = 0; i < dayArray.length; i++) {
	    	if(dayArray[i].getDate().getDayOfWeek() == DayOfWeek.SUNDAY) {
	    		this.weeks.add(dayArray[i].getDate());
	    	}
	    }	
	}

	@Override
	public void changeScreen(Screens name, Shell shell, HashMap<LocalDate, Day> days, InitialValues initialValues) {
		if (name.equals(Screens.WeekLogger)) {
			new WeekLogger(shell, selectedDate, days, initialValues);	
		}
		
	}
}
