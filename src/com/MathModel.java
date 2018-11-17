package com;

import java.math.*;

public class MathModel {
    private BigDecimal[][] Params;
    private BigDecimal[][] refcomParams;
    private BigDecimal h;
    private BigDecimal os;
    private int Accu;
    public int accuCheckStep;
    public long t;
    public boolean autoStep;
    private BigDecimal reqAccu;
    private BigDecimal time;

    public BigDecimal getTime() {
        return time;
    }

    // Velocity in m/s
    // Acceleration in km*1000/s^2
    // Distance in km*1000

    public MathModel(BigDecimal[][] params, boolean _autoStep) {
        autoStep = _autoStep;
        t = 0;
        time=new BigDecimal(0);
        accuCheckStep = 100;
        Accu = 60;
        h = new BigDecimal(10);
        reqAccu = new BigDecimal("1E-15");
        os = new BigDecimal(1).divide(new BigDecimal(6), 30, RoundingMode.FLOOR);
        Params = new BigDecimal[params.length][5];
        for (int i = 0; i < params.length; i++) {
            for (int j = 0; j < 5; j++) {
                Params[i][j] = new BigDecimal(params[i][j].toString().substring(0));
            }
        }
        refcomParams = performStep(Params, h.multiply(new BigDecimal(accuCheckStep)));
    }

    public void setRequiredAccu(BigDecimal reqAccu) {
        this.reqAccu = reqAccu;
    }

    public BigDecimal getStep() {
        return h;
    }

    public void setH(BigDecimal h) {
        this.h = h;
        refcomParams = performStep(Params, h.multiply(new BigDecimal(accuCheckStep)));
    }

    public void setAccu(int accu) {
        Accu = accu;
    }

    public SystemParams getParams() {
        return new SystemParams(Params);
    }

    // 0 - posx
    // 1 - posy
    // 2 - velx
    // 3 - vely

    public int getAccuStatus() {
        if (diff.compareTo(reqAccu) == 1)
            return -1;

        if (diff.multiply(new BigDecimal(16)).compareTo(reqAccu) == -1 && diff.compareTo(BigDecimal.ZERO) != 0)
            return 1;

        return 0;
    }

    BigDecimal diff = new BigDecimal(0);

    public Vector[] getPoints(){
        Vector[] outs = new Vector[Params.length];
        for (int i = 0; i < outs.length; i++) {
            outs[i] = new Vector(Params[i][0], Params[i][1]);
        }

        for (int i = 0; i < Params.length; i++)
            for (int j = 0; j < 4; j++)
                Params[i][j] = Params[i][j].setScale(Accu, RoundingMode.FLOOR);

        return outs;
    }

    public Vector[] Step() {
        Params = performStep(Params, h);
        t++;
        time=time.add(h);
        if (t % accuCheckStep == 0&&t!=0) {
            diff = new BigDecimal(0);
            for (int i = 0; i < Params.length; i++) {
                for (int j = 0; j < 4; j++) {
                    diff = diff.add(Params[i][j].subtract(refcomParams[i][j]).multiply(Params[i][j].subtract(refcomParams[i][j])));
                }
            }
            diff = diff.divide(h, BigDecimal.ROUND_FLOOR);

            if (autoStep)
                switch (getAccuStatus()) {
                    case 1:
                        h = h.multiply(new BigDecimal("2"));
                        break;
                    case -1:
                        h = h.multiply(new BigDecimal("0.5"));
                        break;
                }
            h = h.stripTrailingZeros();
            refcomParams = performStep(Params, h.multiply(new BigDecimal(accuCheckStep)));
        }
return this.getPoints();

    }

    public BigDecimal[][] performStep(BigDecimal[][] in_Params, BigDecimal step) {
        BigDecimal[][] inParams = new BigDecimal[in_Params.length][5];
        for (int i = 0; i < in_Params.length; i++) {
            for (int j = 0; j < 5; j++) {
                inParams[i][j] = new BigDecimal(new BigInteger(in_Params[i][j].unscaledValue().toByteArray().clone()), in_Params[i][j].scale());
            }
        }
        BigDecimal[][] diff1 = new BigDecimal[inParams.length][4];
        for (int i = 0; i < diff1.length; i++) {
            for (int j = 0; j < 4; j++)
                diff1[i][j] = GetDiff(inParams, i, j);
        }

        BigDecimal[][] par1 = new BigDecimal[inParams.length][5];
        for (int i = 0; i < par1.length; i++) {
            for (int j = 0; j < 4; j++)
                par1[i][j] = inParams[i][j].add(diff1[i][j].multiply(step.multiply(new BigDecimal("0.5"))));
            par1[i][4] = inParams[i][4];
        }

        BigDecimal[][] diff2 = new BigDecimal[inParams.length][4];
        for (int i = 0; i < diff2.length; i++) {
            for (int j = 0; j < 4; j++)
                diff2[i][j] = GetDiff(par1, i, j);
        }

        BigDecimal[][] par2 = new BigDecimal[inParams.length][5];
        for (int i = 0; i < par2.length; i++) {
            for (int j = 0; j < 4; j++)
                par2[i][j] = inParams[i][j].add(diff2[i][j].multiply(step.multiply(new BigDecimal("0.5"))));
            par2[i][4] = inParams[i][4];
        }

        BigDecimal[][] diff3 = new BigDecimal[inParams.length][4];
        for (int i = 0; i < diff3.length; i++) {
            for (int j = 0; j < 4; j++)
                diff3[i][j] = GetDiff(par2, i, j);
        }

        BigDecimal[][] par3 = new BigDecimal[inParams.length][5];
        for (int i = 0; i < par3.length; i++) {
            for (int j = 0; j < 4; j++)
                par3[i][j] = inParams[i][j].add(diff3[i][j].multiply(step));
            par3[i][4] = inParams[i][4];
        }

        BigDecimal[][] diff4 = new BigDecimal[inParams.length][4];
        for (int i = 0; i < diff4.length; i++) {
            for (int j = 0; j < 4; j++)
                diff4[i][j] = GetDiff(par3, i, j);
        }

        BigDecimal[][] diff = new BigDecimal[inParams.length][4];
        for (int i = 0; i < diff.length; i++)
            for (int j = 0; j < 4; j++) {
                diff[i][j] = diff1[i][j];
                diff[i][j] = diff[i][j].add(diff2[i][j].multiply(new BigDecimal(2)));
                diff[i][j] = diff[i][j].add(diff3[i][j].multiply(new BigDecimal(2)));
                diff[i][j] = diff[i][j].add(diff4[i][j]);
                diff[i][j] = diff[i][j].multiply(os);
            }
        for (int i = 0; i < inParams.length; i++) {
            for (int j = 0; j < 4; j++)
                inParams[i][j] = inParams[i][j].add(diff[i][j].multiply(step));
        }
        return inParams;
    }

    private BigDecimal GetDiff(BigDecimal[][] parameters, int planetIndex, int funcIndex) {
        BigDecimal[][] params = parameters;
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
        Vector pos = pos2;
        pos.Multiply(new BigDecimal(-1));
        pos.Apply(pos1);

        BigDecimal r = pos.X.multiply(pos.X);
        r = r.add(pos.Y.multiply(pos.Y));
        r = sqrt(r, Accu);
        BigDecimal a = mass2.multiply(new BigDecimal(
                "0.66740831313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131313131")
                .setScale(Accu, RoundingMode.FLOOR));
        BigDecimal ee = new BigDecimal("0");
        BigDecimal rr = r.add(ee);
        rr = rr.multiply(rr);
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
