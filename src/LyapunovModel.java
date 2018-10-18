import java.math.*;

import org.apache.commons.math3.linear.*;

public class LyapunovModel {
    MathModel[] models;
    MathModel cModel;
    public long t = 0;
    double[] baseDiff;

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

        baseDiff=retrieveMatrix();
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

    double[] retrieveMatrix() {
        double[][] matrix = new double[models.length][models.length];
        for (int j = 0; j < models.length; j++) {
            for (int k = 0; k < models.length; k++) {
                matrix[j][k] = models[j].getParams().getParams()[k / 4][k % 4].doubleValue() - cModel.getParams().getParams()[k / 4][k % 4].doubleValue();
            }
        }
        RealMatrix rm = MatrixUtils.createRealMatrix(matrix.clone());
        EigenDecomposition ed = new EigenDecomposition(rm.copy());
        return ed.getRealEigenvalues();
    }

    double[] retrievePLs()
    {
        double[] diff = retrieveMatrix().clone();
        double[] evs=new double[diff.length];
        for (int i = 0; i < evs.length; i++) {
            evs[i]=Math.log(Math.abs(diff[i]/baseDiff[i]))/t;
        }
        return evs;
    }
}
