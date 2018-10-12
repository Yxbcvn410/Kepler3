import java.math.*;

public class MathModel {
	private BigDecimal[][] Params;
	private BigDecimal h;
	private BigDecimal os;
	private int Accu;

	// Velocity in m/s
	// Acceleration in km*1000/s^2
	// Distance in km*1000

	public MathModel(BigDecimal[][] params) {
		Accu = 50;
		h = new BigDecimal(10);
		Params = new BigDecimal[params.length][5];

		for (int i = 0; i < Params.length; i++) {
			for (int j = 0; j < 5; j++)
				Params[i][j] = params[i][j];
			// Params[i][2] = Params[i][2].multiply(new BigDecimal(1000));
			// Params[i][3] = Params[i][3].multiply(new BigDecimal(1000));
		}
		os = new BigDecimal(1).divide(new BigDecimal(6), 30, RoundingMode.FLOOR);
	}

	public void setStep(BigDecimal _h) {
		h = _h;
	}

	public void setAccu(int accu) {
		Accu = accu;
	}
	
	public int getAccu() {
		return Accu;
	}
	
	public SystemParams getParams()
	{
		return new SystemParams(Params);
	}

	// 0 - posx
	// 1 - posy
	// 2 - velx
	// 3 - vely

	public Vector[] PerformStep() {
		BigDecimal[][] diff1 = new BigDecimal[Params.length][4];
		for (int i = 0; i < diff1.length; i++) {
			for (int j = 0; j < 4; j++)
				diff1[i][j] = GetDiff(Params, i, j);
		}
		
		BigDecimal[][] par1 = new BigDecimal[Params.length][5];
		for (int i = 0; i < par1.length; i++) {
			for (int j = 0; j < 4; j++)
				par1[i][j] = Params[i][j].add(diff1[i][j].multiply(h.multiply(new BigDecimal("0.5"))));
			par1[i][4]=Params[i][4];
		}
		
		BigDecimal[][] diff2 = new BigDecimal[Params.length][4];
		for (int i = 0; i < diff2.length; i++) {
			for (int j = 0; j < 4; j++)
				diff2[i][j] = GetDiff(par1, i, j);
		}
		
		BigDecimal[][] par2 = new BigDecimal[Params.length][5];
		for (int i = 0; i < par2.length; i++) {
			for (int j = 0; j < 4; j++)
				par2[i][j] = Params[i][j].add(diff2[i][j].multiply(h.multiply(new BigDecimal("0.5"))));
			par2[i][4]=Params[i][4];
		}
		
		BigDecimal[][] diff3 = new BigDecimal[Params.length][4];
		for (int i = 0; i < diff3.length; i++) {
			for (int j = 0; j < 4; j++)
				diff3[i][j] = GetDiff(par2, i, j);
		}
		
		BigDecimal[][] par3 = new BigDecimal[Params.length][5];
		for (int i = 0; i < par3.length; i++) {
			for (int j = 0; j < 4; j++)
				par3[i][j] = Params[i][j].add(diff3[i][j].multiply(h));
			par3[i][4]=Params[i][4];
		}
		
		BigDecimal[][] diff4 = new BigDecimal[Params.length][4];
		for (int i = 0; i < diff4.length; i++) {
			for (int j = 0; j < 4; j++)
				diff4[i][j] = GetDiff(par3, i, j);
		}
		
		BigDecimal[][] diff = new BigDecimal[Params.length][4];
		for(int i = 0; i < diff.length; i++)
			for(int j = 0; j < 4; j++)
			{
				diff[i][j]=diff1[i][j];
				diff[i][j]=diff[i][j].add(diff2[i][j].multiply(new BigDecimal(2)));
				diff[i][j]=diff[i][j].add(diff3[i][j].multiply(new BigDecimal(2)));
				diff[i][j]=diff[i][j].add(diff4[i][j]);
				diff[i][j]=diff[i][j].multiply(os);
			}
		
		for (int i = 0; i < Params.length; i++) {
			for (int j = 0; j < 4; j++)
				Params[i][j] = Params[i][j].add(diff[i][j].multiply(h));
		}
		
		Vector[] outs = new Vector[Params.length];
		for (int i = 0; i < outs.length; i++) {
			outs[i] = new Vector(Params[i][0], Params[i][1]);
		}

		for (int i = 0; i < Params.length; i++)
			for (int j = 0; j < 4; j++)
				Params[i][j] = Params[i][j].setScale(Accu, RoundingMode.FLOOR);

		return outs;
	}

	private BigDecimal GetDiff(BigDecimal[][] params, int planetIndex, int funcIndex) {
		if (funcIndex < 2) {
			return params[planetIndex][funcIndex + 2];
		} else if (funcIndex == 2) {
			BigDecimal ret = new BigDecimal("0");
			for (int i = 0; i < params.length; i++) {
				if (i != planetIndex) {
					ret = ret.add(GetAccel(new Vector(params[planetIndex][0], params[planetIndex][1]),
							new Vector(params[i][0], params[i][1]), params[i][4]).X);
				}
			}
			return ret.multiply(new BigDecimal("0.000001"));
		} else {
			BigDecimal ret = new BigDecimal("0");
			for (int i = 0; i < params.length; i++) {
				if (i != planetIndex) {
					ret = ret.add(GetAccel(new Vector(params[planetIndex][0], params[planetIndex][1]),
							new Vector(params[i][0], params[i][1]), params[i][4]).Y);
				}
			}
			return ret.multiply(new BigDecimal("0.000001"));
		}
	}

	private Vector GetAccel(Vector pos1, Vector pos2, BigDecimal mass2) { // returns acceleration of the planet#1
		Vector pos = pos2.clone();
		pos.Multiply(new BigDecimal(-1));
		pos.Apply(pos1);

		BigDecimal r = pos.X.multiply(pos.X);
		r = r.add(pos.Y.multiply(pos.Y));
		r = sqrt(r, Accu);
		BigDecimal a = mass2.multiply(new BigDecimal(
				"0.66740831313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131")
						.setScale(Accu, RoundingMode.FLOOR));
		BigDecimal ee = new BigDecimal("0.1");
		BigDecimal rr = r.add(ee);
		rr=rr.multiply(rr);
		a = a.divide(rr, RoundingMode.FLOOR);
		Vector va = new Vector(pos.X, pos.Y);
		va.Multiply(a.divide(r, RoundingMode.FLOOR));
		va.Multiply(new BigDecimal(-1));
		return va;// m/s^2
	}

	public static BigDecimal sqrt(BigDecimal in, int scale) {
		BigDecimal sqrt = new BigDecimal(1);
		sqrt.setScale(scale + 3, RoundingMode.FLOOR);
		BigDecimal store = new BigDecimal(in.toString());
		boolean first = true;
		do {
			if (!first) {
				store = new BigDecimal(sqrt.toString());
			} else
				first = false;
			store.setScale(scale + 3, RoundingMode.FLOOR);
			sqrt = in.divide(store, scale + 3, RoundingMode.FLOOR).add(store).divide(BigDecimal.valueOf(2), scale + 3,
					RoundingMode.FLOOR);
		} while (!store.equals(sqrt));
		return sqrt.setScale(scale, RoundingMode.FLOOR);
	}
	
	
}
