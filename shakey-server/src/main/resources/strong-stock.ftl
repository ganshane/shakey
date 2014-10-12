---
layout: post
title:  "强势股票分析${date_title}"
date:   ${date}
categories: stock
---
<table border="1">
 <tr>
 <td>代码</td>
 <td>25天率</td>
 <td>8天率</td>
 <td>3天率</td>
</tr>
<#list stocks as s>
  <tr style="background-color:${s.color}"><td><a href="http://stock.finance.sina.com.cn/usstock/quotes/${s.symbol()}.html" target="_blank">${s.symbol()}</a></td><td>${s.rate1()}</td><td>${s.rate2()}</td><td>${s.rate3()}</td></tr>
</#list>
</table>
