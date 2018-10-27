package com;

import java.math.*;
import java.util.*;
import javax.swing.*;

public class SystemParams {
	private ArrayList<BigDecimal[]> Params;
	public int n;

	// Velocity in m/s
	// Acceleration in m/s^2
	// Distance in km*1000

	public SystemParams(int p, boolean random) {
		n = p;
		Params = new ArrayList<BigDecimal[]>();
		for (int i = 0; i < n; i++) {
			if (random)
				Params.add(generatePlanet());
			else
				Params.add(new BigDecimal[5]);
		}
	}

	public static BigDecimal[] generatePlanet() {
		BigDecimal[] loc = new BigDecimal[5];
		loc[0] = new BigDecimal(String.valueOf((Math.random() - 0.5) * 2 * 120));
		loc[1] = new BigDecimal(String.valueOf((Math.random() - 0.5) * 2 * 120));
		loc[2] = new BigDecimal(String.valueOf((Math.random() - 0.5) * 0.003));
		loc[3] = new BigDecimal(String.valueOf((Math.random() - 0.5) * 0.003));
		loc[4] = new BigDecimal(String.valueOf(Math.random() * 700));
		return loc;
	}

	public SystemParams(BigDecimal[][] params) {
		n = params.length;
		Params = new ArrayList<BigDecimal[]>();
		for (int i = 0; i < n; i++) {
			BigDecimal[] loc = new BigDecimal[5];
			for (int j = 0; j < 5; j++)
				loc[j] = params[i][j];
			Params.add(loc);
		}
	}

	public SystemParams(String s) {
		Scanner sc = new Scanner(s);
		n = sc.nextInt();
		Params = new ArrayList<BigDecimal[]>();
		for (int i = 0; i < n; i++) {
			BigDecimal[] loc = new BigDecimal[5];
			for (int j = 0; j < 5; j++)
				loc[j] = new BigDecimal(sc.next());
			Params.add(loc);
		}
	}

	public BigDecimal[] getPlanet(int ind) {
		BigDecimal[] out = new BigDecimal[5];
		for (int j = 0; j < 5; j++)
			if (Params.get(ind)[j] == null)
				return null;
			else
				out[j] = Params.get(ind)[j];
		return out;
	}

	public void setPlanet(int ind, BigDecimal[] par) {
		if (par == null)
			return;
		for (int j = 0; j < 5; j++) {
			Params.get(ind)[j] = par[j];
		}
	}

	public void addPlanet(BigDecimal[] par) {
		Params.add(par.clone());
		n++;
	}

	public void removePlanet(int indexAt) {
		Params.remove(indexAt);
		n--;
	}

	public void balance() {
		BigDecimal imX = new BigDecimal(0);
		BigDecimal imY = new BigDecimal(0);
		BigDecimal overallMass = new BigDecimal(0);
		for (int i = 0; i < Params.size(); i++) {
			imX = imX.add(Params.get(i)[2].multiply(Params.get(i)[4]));
			imY = imY.add(Params.get(i)[3].multiply(Params.get(i)[4]));
			overallMass = overallMass.add(Params.get(i)[4]);
		}
		imX = imX.divide(overallMass, RoundingMode.FLOOR);
		imY = imY.divide(overallMass, RoundingMode.FLOOR);
		for (int i = 0; i < Params.size(); i++) {
			Params.get(i)[2] = Params.get(i)[2].subtract(imX);
			Params.get(i)[3] = Params.get(i)[3].subtract(imY);
		}
	}

	@Override
	public String toString() {
		String out = String.valueOf(n) + "\n";
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < 5; j++)
				out += Params.get(i)[j] + " ";
			out += "\n";
		}
		return out;
	}

	public BigDecimal[][] getParams() {
		BigDecimal[][] out = new BigDecimal[n][5];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < 5; j++)
				out[i][j] = Params.get(i)[j];
		return out;
	}

	boolean CheckNull() {
		boolean nil = false;
		for (int i = 0; i < Params.size(); i++)
			if (Params.get(i)[0] == null) {
				JOptionPane.showMessageDialog(null, "Error: no info about planet #" + (i + 1), "Error",
						JOptionPane.ERROR_MESSAGE);
				nil = true;
			}
		return nil;
	}

	public Vector getPosition(int ind) {
		return new Vector(Params.get(ind)[0], Params.get(ind)[1]);
	}

	public Vector getVelocity(int ind) {
		return new Vector(Params.get(ind)[2], Params.get(ind)[3]);
	}
}
