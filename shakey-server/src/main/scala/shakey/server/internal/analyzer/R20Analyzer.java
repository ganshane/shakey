package shakey.server.internal.analyzer;

import org.apache.tapestry5.json.JSONArray;
import shakey.server.services.StockAnalyzer;

import java.util.HashMap;

/**
 * 计算那些股票触碰了20日均线
 *
 * @author jcai
 */
public class R20Analyzer implements StockAnalyzer {
    public void processStockDataInOneYear(String symbol, JSONArray data) {
    }

    public void addDataToTemplateAfterAnalysis(HashMap<Object, Object> model) {
    }

    public String getTemplatePath() {
        return null;
    }
}
