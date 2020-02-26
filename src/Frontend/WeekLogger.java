package Frontend;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import Frontend.Day;

public class WeekLogger implements CurrentScreen {
	
	private static Label total = null;
	private InitialValues initialValues; 
	
	public WeekLogger(Shell shell, LocalDate date, HashMap<LocalDate, Day> days, InitialValues initialValues) {
		this.initialValues = initialValues;
		// define label at start
		Composite weekLogger = new Composite(shell, SWT.NONE);
		// magic number for weeklogger columns
		final int weekLoggerColumns = 4;
	    GridLayout gridLayout = new GridLayout(weekLoggerColumns, false);
	    weekLogger.setLayout(gridLayout);
	    Label weekInfoLabel = new Label(weekLogger, SWT.NONE);
	    weekInfoLabel.setText("Week Info");
	    GridData gridD = new GridData(SWT.NONE, SWT.NONE, false, false);
	    gridD.horizontalSpan = weekLoggerColumns - 1;
	    weekInfoLabel.setLayoutData(gridD);
	    Button hint = new Button(weekLogger, SWT.NONE);
	    Image img = Display.getCurrent().getSystemImage(SWT.ICON_QUESTION);
	    hint.setText("Help");
	    gridD = new GridData(SWT.FILL, SWT.FILL, false, false);
	    gridD.widthHint = weekInfoLabel.getSize().y;
	    gridD.heightHint = weekInfoLabel.getSize().y;
	    hint.setLayoutData(gridD);
	    hint.addListener(SWT.MouseDown, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				Utilities.displayMessage(shell, "- After modifying the week press Save\n"
												+ "-Before Print make sure to Save\n"
												+ "then Click Print", 7000);
			}
		});
		ExpandBar bar = new ExpandBar(weekLogger, SWT.V_SCROLL | SWT.FILL);
		gridD = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridD.horizontalSpan = weekLoggerColumns;
		bar.setLayoutData(gridD);
	    WeekInfo week = new WeekInfo(date, days);
	    ArrayList<Composite> intervals = new ArrayList<Composite>();
	    ArrayList<ExpandItem> exItems = new ArrayList<ExpandItem>();
	    for (int i = 0; i < 7; i++) {
			intervals.add(new Composite(bar, SWT.NONE));
			intervals.get(i).setLayout(new FillLayout());
			intervals.get(i).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			exItems.add(new ExpandItem (bar, SWT.NONE, i));
			newChart(exItems.get(i), intervals.get(i), week.getDay(i));
			exItems.get(i).setText(week.getDay(i).getDate().toString());
			exItems.get(i).setHeight(intervals.get(i).computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
			exItems.get(i).setControl(intervals.get(i));
	    }
		Button submit = new Button(weekLogger, SWT.PUSH);
		submit.addListener(SWT.MouseDown, new Listener() {
			
			@Override
			public void handleEvent(Event e) {
				for(int i = 0; i < 7; i++) {
					Composite chart = (Composite) intervals.get(i).getChildren()[0];
					Composite rows = null;
					for (Control child : chart.getChildren()) {
						if (child instanceof Composite) {
							rows = (Composite) child;
						}
					}
					ArrayList<Interval> intervals = new ArrayList<Interval>();
					for (int intervalIndex = 0; intervalIndex < rows.getChildren().length; intervalIndex++) {
						Composite compInterval = (Composite) rows.getChildren()[intervalIndex];
						Interval interval = new Interval();
						for(int index = 0; index < compInterval.getChildren().length; index++) {
							switch (index) {
								case 0:
									Combo shift = (Combo) compInterval.getChildren()[index];
									interval.setShift(shift.getText());
									break;
								case 1:
									Composite begin = (Composite) compInterval.getChildren()[index];
									Spinner beginHour = (Spinner) begin.getChildren()[0];
									Spinner beginMinute = (Spinner) begin.getChildren()[2];
									interval.setStartTime(LocalTime.of(
																Integer.parseInt(beginHour.getText()),
																Integer.parseInt(beginMinute.getText())
																));
									break;
								case 2:
									Composite end = (Composite) compInterval.getChildren()[index];
									Spinner endHour = (Spinner) end.getChildren()[0];
									Spinner endMinute = (Spinner) end.getChildren()[2];
									interval.setEndTime(LocalTime.of(
											Integer.parseInt(endHour.getText()),
											Integer.parseInt(endMinute.getText())
											));
							}
						}
						intervals.add(interval);
					}
					week.setDay(i, new Day(week.getDay(i).getDate(), intervals));
				}
				try {
					saveWeek(week, days);
					total.setText("Total: $" + calculateTotal(week));
					Utilities.displayMessage(shell, "Saved", 1000);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		submit.setText("Save");
		// cancel button
		Button cancel = new Button(weekLogger, SWT.PUSH);
		cancel.addListener(SWT.MouseDown, new Listener() {
			
			@Override
			public void handleEvent(Event e) {
				weekLogger.dispose();
				changeScreen(Screens.Main, shell, days, initialValues);
			}
		});
		cancel.setText("Back");
		// total text
		total = new Label(weekLogger, SWT.NONE);
		total.setText("Total: $" + calculateTotal(week));
		total.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		// print
		Button print = new Button(weekLogger, SWT.PUSH);
		print.addListener(SWT.MouseDown, new Listener() {
			
			@Override
			public void handleEvent(Event e) {
				try {
					print(shell, week);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		print.setText("Print");
		// redraw the new composite in the shell
		Point windowSize = weekLogger.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		shell.setSize(500, windowSize.y + 50);
		shell.layout();
	}
	
	// OO based methods
	
	private void saveWeek(WeekInfo week, HashMap<LocalDate, Day> days) throws IOException {
		File fout = new File("assets/data.txt");
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	 
		for (int i = 0; i < 7; i++) {
			days.put(week.getDay(i).getDate(), week.getDay(i));
		}
		Day[] dayArray = days.values().toArray(new Day[0]);
		for(int i = 0; i < days.size(); i++) {
			bw.write(dayArray[i].toString());
			bw.newLine();
		}
	 
		bw.close();
	}
	
	@SuppressWarnings("deprecation")
	private void print(Shell shell, WeekInfo week) throws IOException {
		File file = new File("assets/" + week.getDay(0).getDate().toString() + ".pdf");
		PDDocument document = new PDDocument();
		PDPage weekSummary = new PDPage();
		document.addPage(weekSummary);
		try {
			@SuppressWarnings("resource")
			PDPageContentStream contents = new PDPageContentStream(document, weekSummary);
			int xPos = 100;
			int yPos = 700;
			// title
			pdfElement(contents, xPos, yPos, PDType1Font.HELVETICA_BOLD, 12, "Week Summary for " + week.getDay(0).getDate().toString());
	        // basic info
	        yPos -= 25;
			pdfElement(contents, xPos, yPos, PDType1Font.HELVETICA_BOLD_OBLIQUE, 11, "Basic Information");
			// Pay Rate
	        yPos -= 15;
	        pdfElement(contents, xPos, yPos, PDType1Font.HELVETICA, 11, "Pay Rate: $" + this.initialValues.getWage());
	        // Overtime Pay Rate
	        yPos -= 15;
	        pdfElement(contents, xPos, yPos, PDType1Font.HELVETICA, 11, "Overtime Pay Rate: $" + this.initialValues.getWage() * 1.5);
	        // divider
	        yPos -= 20;
	        contents.drawLine(100, yPos, weekSummary.getMediaBox().getWidth() - 100, yPos);
	        int dayFontSize = 9;
	        int colMargin = 6;
	        for(int i = 0; i < 7; i++) {
	        	int tableSize = 0;
	        	if (!week.getDay(i).isWorkEmpty()) {
	        		tableSize = ((dayFontSize + colMargin) * week.getDay(i).getIntervals().size() + 1) + 20;
	        	}
	        	if (yPos - 115 - 20 - tableSize  < 0) {
	        		contents.close();
	        		weekSummary = new PDPage();
	        		document.addPage(weekSummary);
	        		contents = new PDPageContentStream(document, weekSummary);
	        		xPos = 100;
	        		yPos = 700;
	        	}
	        	
	        	yPos = displayDay(weekSummary, contents, xPos, yPos, week.getDay(i), dayFontSize, colMargin);
	            yPos -= 20;
	            contents.drawLine(100, yPos, weekSummary.getMediaBox().getWidth() - 100, yPos);
	        }
	        yPos -= 20;
	        pdfElement(contents, (int) weekSummary.getMediaBox().getWidth() - 235, yPos, PDType1Font.HELVETICA_BOLD, 11, "Week Total: $" + calculateTotal(week));
	        contents.close();
	        try {
	        	document.save(file);
		        Desktop.getDesktop().open(file);
//		        PrinterJob job = PrinterJob.getPrinterJob();
//		        job.setPageable(new PDFPageable(document));
//		        if (job.printDialog()) {
//			        job.print();
//		        }
	        } catch(java.io.FileNotFoundException e) {
	        	Utilities.displayMessage(shell, "You must close the PDF viewer\n and click Print again", 5000);
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
//		catch (PrinterException pe) {
//			pe.printStackTrace();
//		}
	}
	
	private void pdfElement(PDPageContentStream contents, int xoffset, int yoffset, PDFont font, int size, String message) throws IOException {
        contents.beginText();
        contents.setFont(font, size);
        contents.newLineAtOffset(xoffset, yoffset);
        contents.showText(message);
        contents.endText();	
    }
	
	private int displayDay(PDPage weekSummary, PDPageContentStream contents, int xPos, int yPos, Day day, int fontSize, int colMargin) throws IOException {
		int space = 15;
		PDFont font = PDType1Font.HELVETICA;
		yPos -= 20;
        pdfElement(contents, xPos, yPos, font, fontSize, "Day: " + day.getDate().toString());
        double[] norm = day.getNormalPay(this.initialValues.getWage());
        double[] over = day.getOverTimePay(this.initialValues.getWage());
        yPos -= space;
        pdfElement(contents, xPos, yPos, font, fontSize, "Normal Hour: " + norm[0]);
        yPos -= space;
        pdfElement(contents, xPos, yPos, font, fontSize, "Normal Pay: $" + String.format("%.02f", norm[1]));
        yPos -= space;
        pdfElement(contents, xPos, yPos, font, fontSize, "Overtime Hour: " + over[0]);
        yPos -= space;
        pdfElement(contents, xPos, yPos, font, fontSize, "Overtime Pay: $" + String.format("%.02f", over[1]));
        yPos -= space;
        pdfElement(contents, (int) weekSummary.getMediaBox().getWidth() - 200, yPos, PDType1Font.HELVETICA, fontSize, "Total: $" + Utilities.roundMoneyAsString(norm[1] + over[1]));
        yPos -= space;
        if (!day.isWorkEmpty()) {
            pdfElement(contents, xPos, yPos, font, fontSize, "Working Times");
            yPos -= 20;
            yPos = intervalTable(contents, xPos, yPos, day, font, fontSize, colMargin);
        }
		return yPos;
	}
	
	private int intervalTable(PDPageContentStream contents, int xPos, int yPos, Day day, PDFont font, int fontSize, int colMargin) throws IOException {
		// table header
		contents.addRect(xPos, yPos, 100, fontSize + colMargin);
		pdfElement(contents, xPos + 100 / 2 - (int) (font.getStringWidth("Shift") / 200), yPos + colMargin / 2, font, fontSize, "Shift");
		xPos += 100;
		contents.addRect(xPos, yPos, 75, fontSize + colMargin);
		pdfElement(contents, xPos + 75 / 2 - (int) (font.getStringWidth("Start Time") / 200), yPos + colMargin / 2, font, fontSize, "Start Time");
		xPos += 75;
		contents.addRect(xPos, yPos, 75, fontSize + colMargin);
		pdfElement(contents, xPos + 75 / 2 - (int) (font.getStringWidth("End Time") / 200), yPos + colMargin / 2, font, fontSize, "End Time");
		xPos += 75;
		contents.addRect(xPos, yPos, 100, fontSize + colMargin);
		pdfElement(contents, xPos + 100 / 2 - (int) (font.getStringWidth("Duration") / 200), yPos + colMargin / 2, font, fontSize, "Duration");
		//data
		ArrayList<Interval> intervals = day.getIntervals();
		for (int i = 0; i < intervals.size(); i++) {
			yPos -= fontSize + colMargin;
			xPos -= 250;
			contents.addRect(xPos, yPos, 100, fontSize + colMargin);
			pdfElement(contents, xPos + 100 / 2 - (int) (font.getStringWidth(intervals.get(i).getShift()) / 200), yPos + colMargin / 2, font, fontSize, intervals.get(i).getShift());
			xPos += 100;
			contents.addRect(xPos, yPos, 75, fontSize + colMargin);
			pdfElement(contents, xPos + 75 / 2 - (int) (font.getStringWidth(intervals.get(i).getStartTime().toString()) / 200), yPos + colMargin / 2, font, fontSize, intervals.get(i).getStartTime().toString());
			xPos += 75;
			contents.addRect(xPos, yPos, 75, fontSize + colMargin);
			pdfElement(contents, xPos + 75 / 2 - (int) (font.getStringWidth(intervals.get(i).getEndTime().toString()) / 200), yPos + colMargin / 2, font, fontSize, intervals.get(i).getEndTime().toString());
			xPos += 75;
			contents.addRect(xPos, yPos, 100, fontSize + colMargin);
			pdfElement(contents, xPos + 100 / 2 - (int) (font.getStringWidth(Double.toString(intervals.get(i).getHours())) / 200), yPos + colMargin / 2, font, fontSize, Double.toString(intervals.get(i).getHours()));
		}
		return yPos;
	}
		
	private String calculateTotal(WeekInfo week) {
		double total = 0;
		for(int i = 0; i < 7; i++) {
			total += week.getDay(i).getNormalPay(this.initialValues.getWage())[1]
					+ week.getDay(i).getOverTimePay(this.initialValues.getWage())[1];
		}		
		return Utilities.roundMoneyAsString(total);
	}
	
	// GUI based methods

	public void newChart(ExpandItem ei, Composite parent, Day day) {
		Composite chart = new Composite(parent, SWT.NONE);
		chart.setLayout(new GridLayout(3, true));
		Label shift = new Label(chart, SWT.NONE);
		shift.setText("Shift");
		shift.setLayoutData(new GridData(SWT.CENTER, SWT.NONE, true, false));
		Label start = new Label(chart, SWT.NONE);
		start.setText("Start Time");
		start.setLayoutData(new GridData(SWT.CENTER, SWT.NONE, true, false));
		Label end = new Label(chart, SWT.NONE);
		end.setText("End Time");
		end.setLayoutData(new GridData(SWT.CENTER, SWT.NONE, true, false));
		Composite rows = new Composite(chart, SWT.NONE);
		rows.setLayout(new GridLayout(3, false));
		GridData gridD = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridD.horizontalSpan = 3;
		rows.setLayoutData(gridD);
		if (day.getIntervals().size() != 0) {
			ArrayList<Interval> intervals = day.getIntervals();
			for(int i = 0; i < intervals.size(); i++) {
				newRow(rows, ei, parent, intervals.get(i));
			}
		} else {
			newRow(rows, ei, parent, new Interval());
		}
		Button addRow = new Button(chart, SWT.PUSH);
		addRow.setText("Add Row");
		addRow.addListener(SWT.MouseDown, new Listener() {
			
			@Override
			public void handleEvent(Event e) {
				newRow(rows, ei, parent, new Interval());
				ei.setHeight(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
				rows.layout();
			}
		});
	}
	
	public void newRow(Composite parent, ExpandItem ei, Composite chart, Interval interval) {
		Composite row = new Composite(parent, SWT.NONE);
		row.setLayout(new GridLayout(3, true));
		GridData gridD = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridD.horizontalSpan = 3;
		row.setLayoutData(gridD);
		Combo shift = new Combo(row, SWT.READ_ONLY);
		shift.setItems("PRELOAD", "DAY", "TWILIGHT", "MIDNIGHT");
		shift.setText(interval.getShift());
		shift.setLayoutData(new GridData(SWT.CENTER, SWT.NONE, true, false));
		shift.setText(interval.getShift());
		Composite beginTime = newTime(row, false, ei, chart, parent, interval.getStartTime());
		beginTime.setLayoutData(new GridData(SWT.CENTER, SWT.NONE, true, false));
		Composite endTime = newTime(row, true, ei, chart, parent, interval.getEndTime());
		endTime.setLayoutData(new GridData(SWT.CENTER, SWT.NONE, true, false));
	}
	
	public Composite newTime(Composite parent, boolean clearButton, ExpandItem ei, Composite chart, Composite rows, LocalTime time) {
		Composite timeComp = new Composite(parent, SWT.NONE);
		if (clearButton) {
			timeComp.setLayout(new GridLayout(4, false));
		} else {
			timeComp.setLayout(new GridLayout(3, false));
		}
		Spinner hour = new Spinner(timeComp, SWT.NONE);
		hour.setMinimum(0);
		hour.setMaximum(23);
		hour.setLayoutData(new GridData(SWT.NONE, SWT.NONE, true, false));
		hour.setSelection(time.getHour());
		Label colon = new Label(timeComp, SWT.NONE);
		colon.setText(":");
		colon.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false));
		Spinner minute = new Spinner(timeComp, SWT.NONE);
		minute.setMinimum(0);
		minute.setMaximum(59);
		minute.setLayoutData(new GridData(SWT.NONE, SWT.NONE, true, false));
		minute.setSelection(time.getMinute());
		if(clearButton) {
			Button deleteRow = new Button(timeComp, SWT.PUSH);
			deleteRow.setText("X");
			deleteRow.setBackground(new Color(Display.getCurrent(), new RGB(255, 0, 0)));
			deleteRow.addListener(SWT.MouseDown, new Listener() {
				
				@Override
				public void handleEvent(Event arg0) {
					parent.dispose();
					ei.setHeight(chart.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
					rows.layout();
				}
			});
		}
		return timeComp;
	}

	@Override
	public void changeScreen(Screens name, Shell shell, HashMap<LocalDate, Day> days, InitialValues initialValues) {
		if (name.equals(Screens.Main)) {
			new MainScreen(shell, days, initialValues);
		}
		
	}
}
