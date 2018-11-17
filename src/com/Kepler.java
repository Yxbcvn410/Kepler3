package com;

import javax.swing.*;

public class Kepler {

	public static void main(String[] args) {
		System.out.println("Activated.");
		JLabel l = new JLabel("Enter number of planets:");
		JTextArea pn = new JTextArea("3");
		JCheckBox cb = new JCheckBox("Init with random values");
		cb.setSelected(true);
		JCheckBox cb2 = new JCheckBox("Perform Lyapunov coefficient calculation");
		cb2.setSelected(true);
		JComponent[] controls = new JComponent[] { l, pn, cb, cb2 };
		int n = 0;
		boolean init = false;
		boolean lyap = false;
		boolean flag = true;
		while (flag) {
			int result = JOptionPane.showConfirmDialog(null, controls, "Init parameters", JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				try {
					n = Integer.parseInt(pn.getText());
					init = cb.isSelected();
					lyap = cb2.isSelected();
					flag = false;
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Error: Wrong number format", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} else
				return;
		}

		FrameUI fui = new FrameUI(n, init, lyap);
		fui.setVisible(true);
		fui.RefreshPreview();
		while (true){}
	}
}
