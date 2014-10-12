package shakey.server.internal.analyzer;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import shakey.server.services.StockAnalyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 出现天量股票分析.
 * 分析依据，当日的成交量，是之前10天的1.618倍
 *
 * @author jcai
 */
public class DayVolumeAnalyzer implements StockAnalyzer {
    public void processStockDataInOneYear(String symbol, JSONArray data) {
        int pos = data.length() - 1;
        JSONObject current = data.getJSONObject(pos);
        //计算过去十天的均量
        int total = 0;
        int size = 10;
        int untilPos = pos - 1 - size;
        for (int i = pos - 1; i > untilPos; i--) {
            JSONObject obj = data.getJSONObject(i);
            total += obj.getInt("v");
        }
        int av = total / size;
        int currentVolume = current.getInt("v");
        if (currentVolume > av * 2)
            list.add(new VolumeStock(symbol, currentVolume * 1.0 / av));
    }

    public void addDataToTemplateAfterAnalysis(HashMap<Object, Object> model) {
        VolumeStock[] arr = list.toArray(new VolumeStock[list.size()]);
        Arrays.sort(arr);
        model.put("stocks", arr);
    }

    public String getTemplatePath() {
        return "day-volume.ftl";
    }

    private List<VolumeStock> list = new ArrayList<VolumeStock>();

    public static class VolumeStock implements Comparable<VolumeStock> {
        private String symbol;
        private double rate;

        public VolumeStock(String symbol, double rate) {
            this.symbol = symbol;
            this.rate = rate;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public double getRate() {
            return rate;
        }

        public void setRate(double rate) {
            this.rate = rate;
        }

        public int compareTo(VolumeStock o) {
            return Double.compare(o.rate, rate);
        }
    }
}
