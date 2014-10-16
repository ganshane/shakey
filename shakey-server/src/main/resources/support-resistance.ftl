---
layout: post
title:  "触碰支撑位和阻力位股票分析${date_title}"
date:   ${date}
categories: stock
---
<table border="1">
 <tr>
 <td>代码</td>
 <td>接近程度</td>
 <td>阻力1</td>
 <td>阻力2</td>
 <td>支撑1</td>
 <td>支撑2</td>
</tr>
<#list stocks as s>
  <tr>
  <td><a href="http://stock.finance.sina.com.cn/usstock/quotes/${s.symbol()}.html" target="_blank">${s.symbol()}</a></td><td>${s.rate()}</td><td>${s.upSupport()}</td><td>${s.upResistance()}</td><td>${s.downSupport()}</td><td>${s.downResistance()}</td></tr>
</#list>
</table>
