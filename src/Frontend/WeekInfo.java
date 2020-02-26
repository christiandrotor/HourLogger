package Frontend;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

public class WeekInfo {
	private ArrayList<Day> days;
	
	public WeekInfo(LocalDate date, HashMap<LocalDate, Day> dayMap) {
		days = new ArrayList<Day>();
	    while (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
	      date = date.minusDays(1);
	    }
		for (int i = 0; i < 7; i++) {
			if (dayMap.get(date) != null) {
				days.add(dayMap.get(date.plus(i, ChronoUnit.DAYS)));
			} else {
				days.add(new Day(date.plus(i, ChronoUnit.DAYS)));
			}
		}
	}
	
	public Day getDay(int index) {
		return days.get(index);
	}
	
	public void setDay(int index, Day day) {
		days.set(index, day);
	}
}

class Day {
	private ArrayList<Interval> workingTimes;
	private LocalDate date;
	private double hours;

	public Day(LocalDate date) {
		workingTimes = new ArrayList<Interval>();
		this.date = date;
		this.hours = 0.0;
	}
	
	public Day(LocalDate date, ArrayList<Interval> intervals) {
		workingTimes = intervals;
		this.date = date;
		this.hours = 0.0;
		for(int i = 0; i < workingTimes.size(); i++) {
			this.hours += workingTimes.get(i).getHours();
		}
	}
	
	public double[] getNormalPay(double rate) {
		if (this.hours < 5) {
			return new double[] {this.hours, this.hours * rate};
		} else {
			return new double[] {5.0, 5.0 * rate};
		}
	}
	
	public double[] getOverTimePay(double rate) {
		if (this.hours < 5) {
			return new double[] {0.0, 0.0};
		} else {
			double oTime = this.hours - 5.0;
			return new double[] {oTime, oTime * rate * 1.5};
		}
	}
			
	public LocalDate getDate() {
		return date;
	}
	
	public ArrayList<Interval> getIntervals() {
		return workingTimes;
	}
	
	public boolean isWorkEmpty() {
		return workingTimes.size() == 1
			   && workingTimes.get(0).getShift().equals("")
			   && workingTimes.get(0).getStartTime().toString().equals("00:00")
			   && workingTimes.get(0).getEndTime().toString().equals("00:00");
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(date.toString() + ",");
		for(int i = 0; i < workingTimes.size(); i++) {
			Interval interval = workingTimes.get(i);
			sb.append(interval.getShift() + ",");
			sb.append(interval.getStartTime() + ",");
			sb.append(interval.getEndTime() + ",");
		}
		return sb.toString();
	}
}

class Interval {
	private LocalTime startTime;
	private LocalTime endTime;
	private String shift;
	
	public Interval() {
		this.startTime = LocalTime.of(0, 0);
		this.endTime = LocalTime.of(0, 0);
		this.shift = "";
	}
	public LocalTime getStartTime() {
		return startTime;
	}
	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}
	public LocalTime getEndTime() {
		return endTime;
	}
	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}
	public String getShift() {
		return shift;
	}
	public void setShift(String shift) {
		this.shift = shift;
	}
	
	public double getHours() {
		double time = this.endTime.getHour() - this.startTime.getHour();
		time += (this.endTime.getMinute() - this.startTime.getMinute()) / 60.0;
		return time;
	}
}