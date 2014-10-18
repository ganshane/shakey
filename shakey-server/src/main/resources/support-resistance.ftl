---
layout: post
title:  "触碰支撑位或阻力位股票分析${date_title}"
date:   ${date}
categories: stock
---

<script type="text/javascript">
var stockList = []
<#list stocks as s>
stockList.push('gb_${s.symbol()?lower_case}');
</#list>
</script>
<table border="1">
 <tr>
 <td>代码</td>
 <td>最新价</td>
 <td>涨跌幅</td>
 <td>接近程度</td>
 <td>阻力1</td>
 <td>阻力2</td>
 <td>支撑1</td>
 <td>支撑2</td>
</tr>
<#list stocks as s>
  <tr id="${s.symbol()?lower_case}">
  <td><a href="http://stock.finance.sina.com.cn/usstock/quotes/${s.symbol()}.html" target="_blank">${s.symbol()}</a></td><td></td><td></td><td>${s.rate()}</td><td>${s.upSupport()}</td><td>${s.upResistance()}</td><td>${s.downSupport()}</td><td>${s.downResistance()}</td></tr>
</#list>
</table>
