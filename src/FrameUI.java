import javax.swing.*;
import javax.swing.text.JTextComponent;

import java.awt.*;
import java.awt.event.*;
import java.math.*;
import java.sql.Ref;

public class FrameUI extends JFrame {
	BigDecimal[][] Params;
	JTextArea step;
	JTextArea accu;
	Canvas canvas;
	int size;
	boolean flag;
	protected JButton startButton;

	public FrameUI(int n) {
		this.setTitle("Kepler v3.2");
		flag = false;
		size = 800;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.anchor = GridBagConstraints.CENTER;
		gc.gridheight = n + 12;
		gc.gridwidth = 2;
		gc.gridheight = 1;
		gc.gridwidth = 2;
		gc.gridx = 0;
		gc.weightx = 0.5;
		gc.weighty = 0.5;
		gc.insets = new Insets(3, 3, 3, 3);
		gc.fill = GridBagConstraints.BOTH;

		Params = new BigDecimal[n][5];
		for (int ii = 0; ii < n; ii++) {
			Params[ii] = new BigDecimal[5];
			int i = ii;
			gc.gridy = i;
			JButton setParams = new JButton("Specify #" + (i + 1) + " planet");
			setParams.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (Params[i][0] == null)
						Params[i] = getLaunchParams(null, i + 1);
					else
						Params[i] = getLaunchParams(Params[i], i + 1);
				}
			});
			this.add(setParams, gc);
		}

		gc.gridy++;
		gc.gridwidth = 1;
		JLabel ac = new JLabel("Algorythm step (s)");
		this.add(ac, gc);

		gc.gridx = 1;
		step = new JTextArea("10");
		this.add(step, gc);

		gc.gridy++;
		gc.gridx = 0;
		gc.gridwidth = 1;
		JLabel pc = new JLabel("Number accuracy");
		this.add(pc, gc);

		gc.gridx = 1;
		accu = new JTextArea("50");
		this.add(accu, gc);

		gc.gridy++;
		gc.gridx = 0;
		JButton previewButton = new JButton("Preview");
		previewButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				flag = false;
				canvas.getGraphics().clearRect(0, 0, size, size);
				startButton.setText("Start");
				RefreshPreview();
			}
		});
		this.add(previewButton, gc);

		gc.gridx = 1;
		startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (CheckNull())
					return;

				if (flag) {
					flag = false;
					canvas.getGraphics().clearRect(0, 0, size, size);
					startButton.setText("Start");
				} else {
					try {
						BigDecimal h = new BigDecimal(step.getText());
						int a = Integer.parseInt(accu.getText());
						flag = true;
						startButton.setText("Stop");
						canvas.getGraphics().clearRect(0, 0, size, size);
						Thread th = new Thread() {
							public void run() {
								onStarted(h, a);
							}
						};
						th.start();

					} catch (NumberFormatException ee) {
						JOptionPane.showMessageDialog(null, "Error: wrong number format", "Error",
								JOptionPane.OK_OPTION);
						flag = false;
					}
				}
			}
		});
		this.add(startButton, gc);

		gc.gridy++;
		gc.gridx = 0;
		JButton RandomizeButton = new JButton("Randomize values");
		RandomizeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				randomizeValues();
				JOptionPane.showMessageDialog(null, "Successful", "Randomize", JOptionPane.INFORMATION_MESSAGE);
				RefreshPreview();
			}
		});
		this.add(RandomizeButton, gc);

		gc.gridx=1;
		JButton BalanceButton = new JButton("Balance values");
		BalanceButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (CheckNull())
					return;

				balanceValues();
				JOptionPane.showMessageDialog(null, "Successful", "Balance", JOptionPane.INFORMATION_MESSAGE);
				RefreshPreview();
			}
		});
		this.add(BalanceButton, gc);

		gc.gridy++;
		gc.gridx = 0;
		gc.gridwidth = 2;
		gc.gridheight = 10;
		gc.fill = GridBagConstraints.NONE;
		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);
		canvas.setSize(size, size);
		this.add(canvas, gc);

		this.pack();
	}

	void onStarted(BigDecimal h, int a) {
		MathModel model = new MathModel(Params);
		model.setStep(h);
		model.SetAccu(a);
		int rStep = (int) Math.pow(10, 2 - (int) Math.log10(h.doubleValue()));
		if (rStep > 1000)
			rStep = 1000;
		int i = 0;
		while (flag) {
			i++;
			if (i > rStep) {
				DrawPoints(model.PerformStep());
				i -= rStep;
			} else
				model.PerformStep();
		}
	}

	void DrawPoints(Vector[] points) {
		Graphics g = canvas.getGraphics();
		for (int i = 0; i < points.length; i++) {
			float colorID = i / (float) (points.length);
			g.setColor(Color.getHSBColor(colorID, 1, 1));
			g.fillOval(size / 2 + points[i].x - 2, size / 2 - points[i].y - 2, 1, 1);
		}
	}

	void RefreshPreview() {
		Graphics g = canvas.getGraphics();
		g.clearRect(0, 0, size, size);
		for (int i = 0; i < Params.length; i++) {
			try {
				flag = false;
				float colorID = i / (float) (Params.length);
				g.setColor(Color.getHSBColor(colorID, 1, 1));
				Vector Position = new Vector(Params[i][0], Params[i][1]);
				Vector Speed = new Vector(Params[i][2], Params[i][3]);
				BigDecimal Mass = Params[i][4];

				g.fillOval(size / 2 + Position.x - 2, size / 2 - Position.y - 2, 5, 5);
				g.drawLine(size / 2 + Position.x, size / 2 - Position.y,
						size / 2 + Position.x + (int) (Speed.X.doubleValue() * 10),
						size / 2 - Position.y - (int) (Speed.Y.doubleValue() * 10));
			} catch (NullPointerException e) {
				JOptionPane.showMessageDialog(null, "Error: no info about planet #" + (i + 1), "Error",
						JOptionPane.ERROR_MESSAGE);
				g.clearRect(0, 0, size, size);
			}
		}
	}

	BigDecimal[] getLaunchParams(BigDecimal[] inParams, int ind) {
		JLabel lposx = new JLabel("Position X (x1000 km)");
		JLabel lposy = new JLabel("Position Y (x1000 km)");
		JLabel lvelx = new JLabel("Velocity X (km/s)");
		JLabel lvely = new JLabel("Velocity Y (km/s)");
		JLabel lmass = new JLabel("Mass (x10^22 kg)");

		JTextArea posx = new JTextArea("");
		JTextArea posy = new JTextArea("");
		JTextArea velx = new JTextArea("");
		JTextArea vely = new JTextArea("");
		JTextArea mass = new JTextArea("");

		if (inParams != null) {
			posx = new JTextArea(inParams[0].toString());
			posy = new JTextArea(inParams[1].toString());
			velx = new JTextArea(inParams[2].toString());
			vely = new JTextArea(inParams[3].toString());
			mass = new JTextArea(inParams[4].toString());
		}

		final JComponent[] inputs = new JComponent[] { lposx, posx, lposy, posy, lvelx, velx, lvely, vely, lmass,
				mass };
		boolean flagg = true;
		BigDecimal[] lParams = new BigDecimal[5];
		do {
			int result = JOptionPane.showConfirmDialog(null, inputs, "Launch parameters of #" + ind,
					JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				try {
					lParams[0] = new BigDecimal(posx.getText());
					lParams[1] = new BigDecimal(posy.getText());
					lParams[2] = new BigDecimal(velx.getText());
					lParams[3] = new BigDecimal(vely.getText());
					lParams[4] = new BigDecimal(mass.getText());

					flagg = false;
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(this, "Wrong number format", "Error", JOptionPane.ERROR_MESSAGE);
				}
			} else if (result == JOptionPane.CLOSED_OPTION) {
				return inParams;
			} else {
				flagg = false;
			}
		} while (flagg);

		return lParams;
	}

	boolean CheckNull() {
		boolean nil = false;
		for (int i = 0; i < Params.length; i++)
			if (Params[i][0] == null) {
				JOptionPane.showMessageDialog(null, "Error: no info about planet #" + (i + 1), "Error",
						JOptionPane.ERROR_MESSAGE);
				nil = true;
			}
		return nil;
	}

	public void balanceValues() {
		BigDecimal imX = new BigDecimal(0);
		BigDecimal imY = new BigDecimal(0);
		for (int i = 0; i < Params.length; i++) {
			imX = imX.add(Params[i][2].multiply(Params[i][4]));
			imY = imY.add(Params[i][3].multiply(Params[i][4]));
		}
		imX = imX.divide(new BigDecimal(Params.length).setScale(30), RoundingMode.FLOOR);
		imY = imY.divide(new BigDecimal(Params.length).setScale(30), RoundingMode.FLOOR);
		for (int i = 0; i < Params.length; i++) {
			Params[i][2] = Params[i][2].subtract(imX.divide(Params[i][4], RoundingMode.FLOOR));
			Params[i][3] = Params[i][3].subtract(imY.divide(Params[i][4], RoundingMode.FLOOR));
		}
	}

	public void randomizeValues() {
		for (int i = 0; i < Params.length; i++) {
			Params[i][0] = new BigDecimal(String.valueOf((Math.random() - 0.5) * 2 * 120));
			Params[i][1] = new BigDecimal(String.valueOf((Math.random() - 0.5) * 2 * 120));
			Params[i][2] = new BigDecimal(String.valueOf((Math.random() - 0.5) * 3));
			Params[i][3] = new BigDecimal(String.valueOf((Math.random() - 0.5) * 3));
			Params[i][4] = new BigDecimal(String.valueOf(Math.random() * 700));
		}
	}
}