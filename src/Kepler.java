import javax.swing.*;
import java.math.*;

public class Kepler {

	public static void main(String[] args) {
		JLabel l = new JLabel("Enter number of planets:");
		JTextArea pn = new JTextArea("3");
		JCheckBox cb = new JCheckBox("Init with random values");
		JComponent[] controls = new JComponent[] { l, pn, cb };
		int n = 0;
		boolean init = false;
		boolean flag = true;
		while (flag) {
			int result = JOptionPane.showConfirmDialog(null, controls, "Init parameters", JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				try {
					n = Integer.parseInt(pn.getText());
					init = cb.isSelected();
					flag = false;
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Error: Wrong number format", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} else
				return;
		}

		FrameUI fui = new FrameUI(n, init);
		fui.setVisible(true);
	}
}
