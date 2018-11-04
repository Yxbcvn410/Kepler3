package com;

import java.math.*;

import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class LyapunovModel {
    MathModel[] models;
    MathModel cModel;
    public long t = 0;
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
                models[i * 4 + j] = new MathModel(ps, true);
                models[i * 4 + j].setAccu(accu);
                models[i * 4 + j].setRequiredAccu(ra);
            }
        cModel = new MathModel(pars.getParams(), true);
        cModel.setAccu(accu);
        cModel.setRequiredAccu(ra);

        baseDiff = retrieveMatrix();
    }

    Vector[][] performStep() {
        Vector[][] out = new Vector[models.length + 1][models.length / 4];
        BigDecimal mTime = cModel.getTime();
        for (int i = 0; i < models.length; i++)
            if (models[i].getTime().compareTo(mTime) == 1)
                mTime = models[i].getTime();
            boolean ch = false;
        while (cModel.getTime().compareTo(mTime) != 0) {
            cModel.Step();
            ch=true;
        }
        for (int i = 0; i < models.length; i++)
            while (models[i].getTime().compareTo(mTime) != 0) {
                models[i].Step();
                ch = true;
            }
        if(!ch)
        {
            cModel.Step();
            for (MathModel model:models) {
                model.Step();
            }
        }
        out[0] = cModel.getPoints();
        for (int i = 1; i < out.length; i++) {
            out[i] = models[i - 1].getPoints();
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
        try {
            EigenDecomposition ed = new EigenDecomposition(rm.copy());
            return ed.getRealEigenvalues();
        } catch (MaxCountExceededException e) {
            return null;
        }
    }

    double[] retrievePLs() {
        if (retrieveMatrix() == null)
            return null;
        double[] diff = retrieveMatrix().clone();
        double[] evs = new double[diff.length];
        for (int i = 0; i < evs.length; i++) {
            evs[i] = Math.log(Math.abs(diff[i] / baseDiff[i])) / cModel.getTime().doubleValue();
        }
        return evs;
    }
}
