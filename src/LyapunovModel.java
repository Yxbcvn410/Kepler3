import java.math.BigDecimal;
import java.math.RoundingMode;

public class LyapunovModel {
	MathModel[] models;
	MathModel cModel;
	public long t = 0;
	BigDecimal[] baseDiffmap;

	public LyapunovModel(SystemParams pars, BigDecimal h, int accu) {
		models = new MathModel[pars.n * 4];
		for (int i = 0; i < pars.n; i++)
			for (int j = 0; j < 4; j++) {
				BigDecimal[][] ps = pars.getParams();
				if (j < 2)
					ps[i][j] = ps[i][j].add(new BigDecimal("0.001"));
				else
					ps[i][j] = ps[i][j].add(new BigDecimal("0.000001"));
				models[i * 4 + j] = new MathModel(ps);
				models[i * 4 + j].setAccu(accu);
				models[i * 4 + j].setStep(h);
			}
		cModel = new MathModel(pars.getParams());
		cModel.setAccu(accu);
		cModel.setStep(h);

		baseDiffmap = new BigDecimal[models.length];
		for (int j = 0; j < models.length / 4; j++) {
			for (int k = 0; k < 4; k++) {
				BigDecimal difference = models[j * 4 + k].getParams().getParams()[j][k]
						.subtract(cModel.getParams().getParams()[j][k]);
				if (difference.compareTo(new BigDecimal("0")) == -1)
					difference.multiply(new BigDecimal("-1"));
				baseDiffmap[j * 4 + k] = difference;
			}
		}

	}

	Vector[][] performStep() {
		Vector[][] out = new Vector[models.length + 1][models.length / 4];
		out[0] = cModel.PerformStep();
		for (int i = 1; i < out.length; i++) {
			out[i] = models[i - 1].PerformStep();
		}
		t++;
		return out;
	}

	Double[] retreiveMatrix() {
		Double[] matrix = new Double[models.length];

		for (int j = 0; j < models.length / 4; j++) {
			for (int k = 0; k < 4; k++) {
				BigDecimal difference = getDifference(models[j * 4 + k], cModel);
				matrix[j * 4 + k] = Math
						.log(difference.divide(baseDiffmap[j * 4 + k], RoundingMode.FLOOR).doubleValue()) / t;
			}
		}

		return matrix;
	}

	BigDecimal getDifference(MathModel m1, MathModel m2) {
		BigDecimal out = new BigDecimal("0");
		for (int i = 0; i < m1.getParams().n; i++) {
			for (int j = 0; j < 4; j++) {
				out = out.add(m1.getParams().getParams()[i][j].subtract(m2.getParams().getParams()[i][j])
						.multiply(m1.getParams().getParams()[i][j].subtract(m2.getParams().getParams()[i][j])));
			}
		}
		return MathModel.sqrt(out, models[0].getAccu());
	}
}
