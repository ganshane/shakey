package shakey.server.internal.analyzer;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import shakey.server.internal.stat.StockAlgorithm;
import shakey.server.services.StockAnalyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 计算那些股票触碰了20日均线
 *
 * @author jcai
 */
public class R20Analyzer implements StockAnalyzer {
    public void processStockDataInOneYear(String symbol, JSONArray data) {
        double r20 = 0.0;
        int pos = data.length() - 1;
        for (int i = 0; i < 20; i++) {
            r20 += data.getJSONObject(pos - i).getDouble("c");
        }
        r20 /= 20;

        JSONObject current = data.getJSONObject(pos);
        double o = current.getDouble("o");
        double c = current.getDouble("c");
        boolean isReach = (Math.abs(o - r20) + Math.abs(r20 - c)) == Math.abs(c - o);
        if (isReach) {
            list.add(new R20Stock(symbol, StockAlgorithm.calStrongRate(data, 25)));
        }
    }

    public String getTemplatePath() {
        return "/r20.ftl";
    }

    public void addDataToTemplateAfterAnalysis(HashMap<Object, Object> model) {
        R20Stock[] arr = list.toArray(new R20Stock[list.size()]);
        Arrays.sort(arr);
        model.put("stocks", arr);
    }

    private List<R20Stock> list = new ArrayList<R20Stock>();

    public static class R20Stock implements Comparable<R20Stock> {
        private String symbol;
        private double strongRate;

        public R20Stock(String symbol, double strongRate) {
            this.symbol = symbol;
            this.strongRate = strongRate;
        }

        public int compareTo(R20Stock o) {
            return Double.compare(o.strongRate, strongRate);
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public double getStrongRate() {
            return strongRate;
        }

        public void setStrongRate(double strongRate) {
            this.strongRate = strongRate;
        }
    }
}
