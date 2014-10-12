---
layout: post
title:  "反转股票${date_title}"
date:   ${date}
categories: stock
---
<table border="1">
 <tr>
 <td>代码</td>
 <td>反转指数</td>
</tr>
<#list stocks as s>
  <tr><td><a href="http://finance.yahoo.com/echarts?s=${s.symbol()}#symbol=${s.symbol()};range=3m" target="_blank">${s.symbol()}</a></td><td>${s.rate()}</td></tr>
</#list>
</table>
