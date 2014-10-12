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
  <tr><td>${s.symbol()}</td><td>${s.rate()}</td></tr>
</#list>
</table>
