package Frontend;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class Utilities {
	
	static void displayMessage(Shell shell, String message, int time) {
		  Shell hoverShell = new Shell(shell, SWT.ON_TOP | SWT.TOOL);
		  hoverShell.setLayout(new FillLayout());
		  Label messageLabel = new Label(hoverShell, SWT.NONE);
		  messageLabel.setBackground(new Color(Display.getCurrent(), new RGB(255, 250, 205)));
		  messageLabel.setText(message);
		  Point shellLocation = shell.getLocation();
		  hoverShell.pack();
		  hoverShell.setLocation(shellLocation.x + (shell.getSize().x / 2) - (hoverShell.getSize().x / 2), shellLocation.y + 40);
		  hoverShell.open();
		  Display.getDefault().timerExec(time, new Runnable() {
			
			@Override
			public void run() {
				hoverShell.dispose();
			}
		});
	}
	
	static String roundMoneyAsString(double money) {
		return Double.toString(Math.round(money * 100.0) / 100.0);
	}
}
