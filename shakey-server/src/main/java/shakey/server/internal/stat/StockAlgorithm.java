package shakey.server.internal.stat;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

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

    /**
     * 计算day_len天数内股价的强度系数
     *
     * @param jsonArray 一串的股票数据, 数据格式如下:
     *                  {d:"2012-11-21",o:"10.50",h:"11.75",l:"10.50",c:"11.31",v:"4567029"}
     * @param day_len   计算的天数
     * @return 股价的强度系数
     */
    public static double calStrongRate(JSONArray jsonArray, int day_len) {
        int len = jsonArray.length();
        int size = day_len;
        int begin = len - size;
        if (begin < 0)
            begin = 0;
        size = len - begin;

        int[] xx = new int[size];
        for (int i = 0; i < size; i++)
            xx[i] = i;

        double[] yy = new double[size];
        for (int i = begin; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            //{d:"2012-11-21",o:"10.50",h:"11.75",l:"10.50",c:"11.31",v:"4567029"}
            double h = obj.getDouble("h");
            double l = obj.getDouble("l");
            yy[i - begin] = Math.log(l + (h - l) / 2);
        }

        return StockAlgorithm.lineSimulate(xx, yy);
    }

    public static double average(double[] data, int from, int until) {
        double total = 0.0;
        int i = from;
        for (; i < until && i < data.length; i++) {
            total += data[i];
        }
        return total / (i - from);
    }
}
