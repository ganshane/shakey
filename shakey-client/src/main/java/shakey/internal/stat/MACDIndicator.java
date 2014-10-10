/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shakey.internal.stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author 18meters
 */
public class MACDIndicator {
    // 参数名定义

    /**
     * 参数名：短期
     */
    private static final String PARAM_SHORT = "SHORT";

    /**
     * 参数名：长期
     */
    private static final String PARAM_LONG = "LONG";

    /**
     * 参数名：天数
     */
    private static final String PARAM_M = "M";

    // 计算值定义
    /**
     * 计算值：差离率
     */
    private static final String VALUE_DIFF = "DIFF";

    /**
     * 计算值：平滑移动平均值
     */
    private static final String VALUE_DEA = "DEA";

    /**
     * 计算值：指数平滑异同移动平均线（上涨）
     */
    private static final String VALUE_RMACD = "RMACD";

    /**
     * 计算值：指数平滑异同移动平均线（下跌）
     */
    private static final String VALUE_FMACD = "FMACD";
    //public DailyMACDIndicator(List<double> stock) {

    // }
    private double getMa(List<Double> stockPrice, int start,int length) {
        double totalClose = 0;
        double close = 0;
        int i = start;
        do {

            close = stockPrice.get(i);//dailyKData.getClose(dateStr);
            totalClose += close;
            i++;
        } while (i < length);
        return totalClose /length;

    }

    /**
     * 计算短期和长期的差离率。
     * <p>
     * 即收盘价短期和长期指数平滑移动平均线之间的差。</p>
     *
     * @params shortEMA 短期指数平滑移动平均线
     * @params longEMA 长期指数平滑移动平均线
     *
     * @return 差离率（DIFF值）
     */
    private double getDIFFValue(
            double shortEMA,
            double longEMA) {

        // 计算差离率（DIFF）
        return shortEMA - longEMA;

    }

    /**
     * 计算出指定股票指定日期范围的指数平滑异同移动平均线值序列。
     *
     * @param stockPrice 股价序列
     * @param paramSHORT 短期
     * @param paramLONG 长期
     * @param paramM DEA周期
     * paramLONG+paramM-1后数据才有效
     * @return 指数平滑异同移动平均线值序列
     */
    private List< Map<String, Double>> calcStkDailyMACD(
            List<Double> stockPrice,
            int paramSHORT,
            int paramLONG,
            int paramM) {
        List<Map<String, Double>> result = new ArrayList<Map<String, Double>>();
        // 得到短期的第一个EMA值（其实为MA值）
        double shortEMA = getMa(stockPrice,paramLONG- paramSHORT,paramSHORT);
        // 得到长期的第一个EMA值（其实为MA值）
        double longEMA = getMa(stockPrice, 0,paramLONG);
        double diffValue = getDIFFValue(shortEMA, longEMA);
        // 计算的第一个diffValue作为DEA的第一个值 
        double deaValue = diffValue;
        // 自起始日期始至终止日期止，依次取得各日的指数平滑异同移动平均线的值
        double close;
        int i = paramLONG;
        double shortSmoth = 2 / (paramSHORT + 1);
        double longSmoth = 2 / (paramLONG + 1);
        do {
            close = stockPrice.get(i);
            Map<String, Double> indicatorValue = new HashMap<String, Double>();
            // 计算短期EMA值
            shortEMA = shortSmoth * close
                    + (paramSHORT - 1) / (paramSHORT + 1) * shortEMA;
            // 计算长期EMA值
            longEMA = longSmoth * close
                    + (paramLONG - 1) / (paramLONG + 1) * longEMA;
            // 计算DIFF值（收盘价短期、长期指数平滑移动平均线间的差）
            diffValue = getDIFFValue(shortEMA, longEMA);
            indicatorValue.put(VALUE_DIFF, diffValue);
            // 计算DEA值（DIFF线的M日指数平滑移动平均线）
            deaValue = 2 / (paramM + 1) * diffValue
                    + (paramM - 1) / (paramM + 1) * deaValue;
            indicatorValue.put(VALUE_DEA, deaValue);
            // 计算MACD值
            double macdValue = 2 * (diffValue - deaValue);
            if (macdValue >= 0) {
                indicatorValue.put(VALUE_RMACD, macdValue); // 上涨MACD
            } else {
                indicatorValue.put(VALUE_FMACD, macdValue); // 下跌MACD
            }
            // 加入一个值对象
            result.add(indicatorValue);
            i++;
        } while (i < stockPrice.size());
        return result;
    }
}
