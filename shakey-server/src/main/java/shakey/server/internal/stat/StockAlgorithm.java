package shakey.server.internal.stat;

/**
 * 股票相关算法
 *
 * @author jcai
 */
public class StockAlgorithm {
    /**
     * 利用直线拟合求出一段时间内股价的趋势
     *
     * @param xx 时间序列
     * @param yy 股价序列,长度要和xx保持一致
     * @return 斜率, 斜率为负值, 则表明股价可能下挫，可以卖空，为正值，则股价上升，可以买入
     */
    public static double lineSimulate(int[] xx, double[] yy) {
        double yysum = 0;
        double xxsum = 0;
        double xxyy = 0;
        double xxxx = 0;

        for (int i = 0; i < xx.length; i++) {
            xxsum += xx[i];
            yysum += yy[i];
            xxyy += xx[i] * yy[i];
            xxxx += xx[i] * xx[i];
        }

        double D = (xxsum * xxsum - xx.length * xxxx);
        double a = (yysum * xxsum - xx.length * xxyy) / D;
        //val b = (xxsum * xxyy - yysum * xxxx) / D

        return a;
    }
}
