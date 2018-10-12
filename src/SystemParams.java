import java.math.*;
import java.util.*;
import javax.swing.*;

public class SystemParams {
	BigDecimal[][] Params;
	int n;

	// Velocity in m/s
	// Acceleration in m/s^2
	// Distance in km*1000

	public SystemParams(int p, boolean random) {
		n = p;
		Params = new BigDecimal[n][5];
		for (int i = 0; i < n; i++) {
			Params[i] = new BigDecimal[5];
			if (random) {
				Params[i][0] = new BigDecimal(String.valueOf((Math.random() - 0.5) * 2 * 120));
				Params[i][1] = new BigDecimal(String.valueOf((Math.random() - 0.5) * 2 * 120));
				Params[i][2] = new BigDecimal(String.valueOf((Math.random() - 0.5) * 0.000003));
				Params[i][3] = new BigDecimal(String.valueOf((Math.random() - 0.5) * 0.000003));
				Params[i][4] = new BigDecimal(String.valueOf(Math.random() * 700));
			}
		}
	}

	public SystemParams(BigDecimal[][] params) {
		n = params.length;
		Params = new BigDecimal[n][5];
		for (int i = 0; i < n; i++) {
			Params[i] = new BigDecimal[5];
			for (int j = 0; j < 5; j++)
				Params[i][j] = params[i][j];
		}
	}

	public SystemParams(String s) {
		Scanner sc = new Scanner(s);
		n = sc.nextInt();
		Params = new BigDecimal[n][5];
		for (int i = 0; i < n; i++) {
			Params[i] = new BigDecimal[5];
			for (int j = 0; j < 5; j++)
				Params[i][j] = new BigDecimal(sc.next());
		}
	}

	public BigDecimal[] getPlanet(int ind) {
		BigDecimal[] out = new BigDecimal[5];
		for (int j = 0; j < 5; j++)
			if (Params[ind][j] == null)
				return null;
			else
				out[j] = Params[ind][j];
		return out;
	}

	public void setPlanet(int ind, BigDecimal[] par) {
		if (par == null)
			return;
		for (int j = 0; j < 5; j++) {
			Params[ind][j] = par[j];
		}
	}

	public void balance() {
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

	@Override
	public String toString() {
		String out = String.valueOf(n) + "\n";
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < 5; j++)
				out += Params[i][j] + " ";
			out += "\n";
		}
		return out;
	}

	public BigDecimal[][] getParams() {
		BigDecimal[][] out = new BigDecimal[n][5];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < 5; j++)
				out[i][j] = Params[i][j];
		return out;
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

	public Vector getPosition(int ind) {
		return new Vector(Params[ind][0], Params[ind][1]);
	}

	public Vector getVelocity(int ind) {
		return new Vector(Params[ind][2], Params[ind][3]);
	}
}
