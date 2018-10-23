import java.math.*;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class LyapunovModel {
    MathModel[] models;
    MathModel cModel;
    public long t = 1;
    public BigDecimal time = BigDecimal.ZERO;
    double[] baseDiff;

    public LyapunovModel(SystemParams pars, BigDecimal ra, int accu) {
        models = new MathModel[pars.n * 4];
        for (int i = 0; i < pars.n; i++)
            for (int j = 0; j < 4; j++) {
                BigDecimal[][] ps = pars.getParams();
                if (j < 2)
                    ps[i][j] = ps[i][j].add(new BigDecimal("0.001"));
                else
                    ps[i][j] = ps[i][j].add(new BigDecimal("0.000001"));
                models[i * 4 + j] = new MathModel(ps, false);
                models[i * 4 + j].setAccu(accu);
                models[i * 4 + j].setRequiredAccu(ra);
            }
        cModel = new MathModel(pars.getParams(), false);
        cModel.setAccu(accu);
        cModel.setRequiredAccu(ra);

        baseDiff=retrieveMatrix();
    }

    Vector[][] performStep() {
        Vector[][] out = new Vector[models.length + 1][models.length / 4];
        out[0] = cModel.Step();
        for (int i = 1; i < out.length; i++) {
            out[i] = models[i - 1].Step();
        }
        t++;
        time.add(cModel.getStep());
        if(t%cModel.accuCheckStep==0)
        {
            int accuSt=cModel.getAccuStatus();
            for (int i = 0; i < models.length; i++) {
                if(models[i].getAccuStatus()<accuSt)
                    accuSt=models[i].getAccuStatus();
            }
            switch (accuSt)
            {
                case 1:
                    cModel.setH(cModel.getStep().multiply(new BigDecimal(2)));
                    for (int i = 0; i < models.length; i++) {
                        models[i].setH(cModel.getStep().multiply(new BigDecimal(2)));
                    }
                    break;
                case -1:
                    cModel.setH(cModel.getStep().multiply(new BigDecimal("0.5")));
                    for (int i = 0; i < models.length; i++) {
                        models[i].setH(cModel.getStep().multiply(new BigDecimal("0.5")));
                    }break;
            }
            System.out.println("Step set to: " + cModel.getStep().toString());
        }
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
