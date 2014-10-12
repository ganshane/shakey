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
  <tr><td><a href="http://stock.finance.sina.com.cn/usstock/quotes/${s.symbol()}.html" target="_blank">${s.symbol()}</a></td><td>${s.rate()}</td></tr>
</#list>
</table>
